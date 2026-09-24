package com.example.pocketpilot.feature.auth.data.remote

import com.example.pocketpilot.feature.auth.data.remote.dto.AuthResponseDto
import com.example.pocketpilot.feature.auth.data.remote.dto.AuthTokensDto
import com.example.pocketpilot.feature.auth.data.remote.dto.UserDto
import com.example.pocketpilot.feature.auth.domain.model.AuthException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * In-memory fake that stands in for a real auth API while the backend is
 * mocked. State is confined behind [mutex] so concurrent screens can safely
 * exercise the flow. Seeded with a demo account for quick sign-in.
 */
class FakeAuthRemoteDataSource(private val simulatedLatencyMillis: Long = 600L) : AuthRemoteDataSource {

    private data class Account(val id: String, val email: String, var password: String, var displayName: String?)

    private val mutex = Mutex()
    private val accountsByEmail = mutableMapOf<String, Account>()
    private val outstandingResetCodes = mutableMapOf<String, String>()

    init {
        val demo = Account(
            id = UUID.randomUUID().toString(),
            email = "demo@pocketpilot.app",
            password = "password1",
            displayName = "Demo User"
        )
        accountsByEmail[demo.email.lowercase()] = demo
    }

    override suspend fun login(email: String, password: String): AuthResponseDto {
        delay(simulatedLatencyMillis)
        val account = mutex.withLock { accountsByEmail[email.lowercase()] }
            ?: throw AuthException.InvalidCredentials
        if (account.password != password) throw AuthException.InvalidCredentials
        return account.toAuthResponse()
    }

    override suspend fun signUp(name: String, email: String, password: String): AuthResponseDto {
        delay(simulatedLatencyMillis)
        val key = email.lowercase()
        return mutex.withLock {
            if (accountsByEmail.containsKey(key)) throw AuthException.EmailAlreadyRegistered
            val account = Account(
                id = UUID.randomUUID().toString(),
                email = email,
                password = password,
                displayName = name
            )
            accountsByEmail[key] = account
            account.toAuthResponse()
        }
    }

    override suspend fun requestPasswordReset(email: String): String {
        delay(simulatedLatencyMillis)
        val key = email.lowercase()
        return mutex.withLock {
            if (!accountsByEmail.containsKey(key)) throw AuthException.AccountNotFound
            val code = (100000..999999).random().toString()
            outstandingResetCodes[key] = code
            code
        }
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): AuthResponseDto {
        delay(simulatedLatencyMillis)
        val key = email.lowercase()
        return mutex.withLock {
            val expected = outstandingResetCodes[key] ?: throw AuthException.InvalidResetCode
            if (expected != code) throw AuthException.InvalidResetCode
            val account = accountsByEmail[key] ?: throw AuthException.AccountNotFound
            account.password = newPassword
            outstandingResetCodes.remove(key)
            account.toAuthResponse()
        }
    }

    private fun Account.toAuthResponse(): AuthResponseDto = AuthResponseDto(
        user = UserDto(id = id, email = email, displayName = displayName),
        tokens = AuthTokensDto(
            accessToken = "access-${UUID.randomUUID()}",
            refreshToken = "refresh-${UUID.randomUUID()}",
            expiresInSeconds = 60L * 60L
        )
    )
}
