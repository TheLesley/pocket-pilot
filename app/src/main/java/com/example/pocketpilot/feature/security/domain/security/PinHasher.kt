package com.example.pocketpilot.feature.security.domain.security

/**
 * Deterministic, salted PIN hasher. Kept as an interface so the domain layer
 * can compose a PIN change without importing the JVM crypto APIs — and tests
 * can substitute a trivial fake.
 */
interface PinHasher {

    /** Generate a fresh, cryptographically-random salt as a hex string. */
    fun newSalt(): String

    /** Hash [pin] with [salt] and return the hex-encoded digest. */
    fun hash(pin: String, salt: String): String
}
