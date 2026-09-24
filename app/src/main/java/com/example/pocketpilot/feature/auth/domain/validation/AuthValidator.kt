package com.example.pocketpilot.feature.auth.domain.validation

sealed class ValidationError(val message: String) {
    data object EmailBlank : ValidationError("Email is required")
    data object EmailInvalid : ValidationError("Enter a valid email address")
    data object PasswordBlank : ValidationError("Password is required")
    data object PasswordTooShort : ValidationError("Password must be at least 8 characters")
    data object PasswordTooWeak : ValidationError("Include a letter and a number")
    data object PasswordMismatch : ValidationError("Passwords do not match")
    data object NameBlank : ValidationError("Name is required")
    data object CodeBlank : ValidationError("Reset code is required")
}

object AuthValidator {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(raw: String): ValidationError? {
        val trimmed = raw.trim()
        return when {
            trimmed.isEmpty() -> ValidationError.EmailBlank
            !emailRegex.matches(trimmed) -> ValidationError.EmailInvalid
            else -> null
        }
    }

    fun validatePassword(raw: String): ValidationError? = when {
        raw.isEmpty() -> ValidationError.PasswordBlank
        raw.length < 8 -> ValidationError.PasswordTooShort
        !raw.any { it.isLetter() } || !raw.any { it.isDigit() } -> ValidationError.PasswordTooWeak
        else -> null
    }

    /** Weaker check for logging in — we only require presence, not policy. */
    fun validateLoginPassword(raw: String): ValidationError? = when {
        raw.isEmpty() -> ValidationError.PasswordBlank
        else -> null
    }

    fun validateName(raw: String): ValidationError? = when {
        raw.trim().isEmpty() -> ValidationError.NameBlank
        else -> null
    }

    fun validatePasswordsMatch(a: String, b: String): ValidationError? = if (a != b) ValidationError.PasswordMismatch else null

    fun validateResetCode(raw: String): ValidationError? = when {
        raw.trim().isEmpty() -> ValidationError.CodeBlank
        else -> null
    }
}
