package com.example.pocketpilot.feature.security.data.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom

class Sha256PinHasherTest {

    private val hasher = Sha256PinHasher(secureRandom = SecureRandom())

    @Test
    fun `newSalt returns 32-character hex string`() {
        val salt = hasher.newSalt()
        assertEquals(32, salt.length)
        assertTrue(salt.all { it in "0123456789abcdef" })
    }

    @Test
    fun `hashing the same pin and salt yields the same digest`() {
        val salt = hasher.newSalt()
        assertEquals(hasher.hash("1234", salt), hasher.hash("1234", salt))
    }

    @Test
    fun `changing the pin changes the digest`() {
        val salt = hasher.newSalt()
        assertNotEquals(hasher.hash("1234", salt), hasher.hash("4321", salt))
    }

    @Test
    fun `changing the salt changes the digest`() {
        val a = hasher.newSalt()
        val b = hasher.newSalt()
        assertNotEquals(hasher.hash("1234", a), hasher.hash("1234", b))
    }
}
