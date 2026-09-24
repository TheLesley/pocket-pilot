package com.example.pocketpilot.feature.auth.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun `validateEmail rejects blank`() {
        assertEquals(ValidationError.EmailBlank, AuthValidator.validateEmail("   "))
    }

    @Test
    fun `validateEmail rejects malformed`() {
        assertEquals(ValidationError.EmailInvalid, AuthValidator.validateEmail("not-an-email"))
    }

    @Test
    fun `validateEmail accepts a valid email`() {
        assertNull(AuthValidator.validateEmail("user@example.com"))
    }

    @Test
    fun `validatePassword rejects short passwords`() {
        assertEquals(ValidationError.PasswordTooShort, AuthValidator.validatePassword("abc12"))
    }

    @Test
    fun `validatePassword rejects passwords without a digit`() {
        assertEquals(ValidationError.PasswordTooWeak, AuthValidator.validatePassword("abcdefgh"))
    }

    @Test
    fun `validatePassword accepts strong password`() {
        assertNull(AuthValidator.validatePassword("secret12"))
    }

    @Test
    fun `validatePasswordsMatch surfaces mismatch`() {
        assertEquals(
            ValidationError.PasswordMismatch,
            AuthValidator.validatePasswordsMatch("a", "b")
        )
    }

    @Test
    fun `validateName rejects blank name`() {
        assertEquals(ValidationError.NameBlank, AuthValidator.validateName("   "))
    }
}
