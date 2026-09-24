package com.example.pocketpilot.feature.security.data.security

import com.example.pocketpilot.feature.security.domain.security.PinHasher
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * SHA-256 salted PIN hasher. A single round is sufficient for a four-to-eight
 * digit PIN whose entropy is dominated by the salt — bcrypt/scrypt would be
 * overkill for a local unlock secret and would add a third-party dependency
 * we do not need. The salt is 16 random bytes, hex-encoded.
 */
class Sha256PinHasher(private val secureRandom: SecureRandom = SecureRandom()) : PinHasher {

    override fun newSalt(): String {
        val bytes = ByteArray(SaltBytes)
        secureRandom.nextBytes(bytes)
        return bytes.toHex()
    }

    override fun hash(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        digest.update(pin.toByteArray(Charsets.UTF_8))
        return digest.digest().toHex()
    }

    private fun ByteArray.toHex(): String = buildString(size * 2) {
        for (byte in this@toHex) {
            val v = byte.toInt() and 0xFF
            append(HexChars[v ushr 4])
            append(HexChars[v and 0x0F])
        }
    }

    private companion object {
        const val SaltBytes = 16
        val HexChars = "0123456789abcdef".toCharArray()
    }
}
