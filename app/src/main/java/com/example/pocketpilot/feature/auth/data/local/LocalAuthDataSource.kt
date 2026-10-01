package com.example.pocketpilot.feature.auth.data.local

import com.example.pocketpilot.feature.auth.data.local.dao.AccountDao
import com.example.pocketpilot.feature.auth.data.local.entity.AccountEntity
import com.example.pocketpilot.feature.auth.data.remote.AuthRemoteDataSource
import com.example.pocketpilot.feature.auth.data.remote.dto.AuthResponseDto
import com.example.pocketpilot.feature.auth.data.remote.dto.AuthTokensDto
import com.example.pocketpilot.feature.auth.data.remote.dto.UserDto
import com.example.pocketpilot.feature.auth.domain.model.AuthException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

/**
 * Offline-first implementation of [AuthRemoteDataSource] backed by Room. The
 * class deliberately keeps the same contract as the fake so the repository
 * layer needs no branching — swapping between "local only" and a live remote
 * happens purely in the DI container.
 *
 * Passwords are stored as salted SHA-256 hashes. A single hash is acceptable
 * here because credentials never leave the device — the on-disk digest cannot
 * be replayed against a server, and the salt frustrates rainbow-table lookups
 * if the database file is ever exfiltrated.
 */
class LocalAuthDataSource(
    private val accountDao: AccountDao,
    private val clock: () -> Long = System::currentTimeMillis,
    private val secureRandom: SecureRandom = SecureRandom(),
) : AuthRemoteDataSource {

    override suspend fun login(email: String, password: String): AuthResponseDto {
        val key = email.trim().lowercase()
        val account = accountDao.findByEmail(key) ?: throw AuthException.InvalidCredentials
        val computed = hash(password, account.salt)
        if (!constantTimeEquals(computed, account.passwordHash)) {
            throw AuthException.InvalidCredentials
        }
        return account.toAuthResponse()
    }

    override suspend fun signUp(name: String, email: String, password: String): AuthResponseDto {
        val key = email.trim().lowercase()
        if (accountDao.findByEmail(key) != null) throw AuthException.EmailAlreadyRegistered
        val salt = newSalt()
        val entity = AccountEntity(
            id = UUID.randomUUID().toString(),
            email = key,
            displayName = name.trim().ifBlank { null },
            passwordHash = hash(password, salt),
            salt = salt,
            createdAtEpochMillis = clock(),
        )
        accountDao.insert(entity)
        return entity.toAuthResponse()
    }

    override suspend fun requestPasswordReset(email: String): String {
        val key = email.trim().lowercase()
        val account = accountDao.findByEmail(key) ?: throw AuthException.AccountNotFound
        val code = (100000..999999).random().toString()
        accountDao.update(account.copy(resetCode = code))
        return code
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): AuthResponseDto {
        val key = email.trim().lowercase()
        val account = accountDao.findByEmail(key) ?: throw AuthException.AccountNotFound
        val expected = account.resetCode ?: throw AuthException.InvalidResetCode
        if (expected != code) throw AuthException.InvalidResetCode
        val salt = newSalt()
        val updated = account.copy(
            passwordHash = hash(newPassword, salt),
            salt = salt,
            resetCode = null,
        )
        accountDao.update(updated)
        return updated.toAuthResponse()
    }

    private fun AccountEntity.toAuthResponse(): AuthResponseDto = AuthResponseDto(
        user = UserDto(id = id, email = email, displayName = displayName),
        tokens = AuthTokensDto(
            accessToken = "local-access-${UUID.randomUUID()}",
            refreshToken = "local-refresh-${UUID.randomUUID()}",
            expiresInSeconds = SESSION_TTL_SECONDS,
        )
    )

    private fun newSalt(): String {
        val bytes = ByteArray(SALT_BYTES)
        secureRandom.nextBytes(bytes)
        return bytes.toHex()
    }

    private fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        digest.update(password.toByteArray(Charsets.UTF_8))
        return digest.digest().toHex()
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var mismatch = 0
        for (i in a.indices) {
            mismatch = mismatch or (a[i].code xor b[i].code)
        }
        return mismatch == 0
    }

    private fun ByteArray.toHex(): String = buildString(size * 2) {
        for (byte in this@toHex) {
            val v = byte.toInt() and 0xFF
            append(HEX_CHARS[v ushr 4])
            append(HEX_CHARS[v and 0x0F])
        }
    }

    private companion object {
        const val SALT_BYTES = 16
        const val SESSION_TTL_SECONDS: Long = 60L * 60L
        val HEX_CHARS = "0123456789abcdef".toCharArray()
    }
}
