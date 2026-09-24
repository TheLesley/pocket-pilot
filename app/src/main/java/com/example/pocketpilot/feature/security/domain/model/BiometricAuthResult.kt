package com.example.pocketpilot.feature.security.domain.model

import androidx.compose.runtime.Immutable

/**
 * Outcome of a single biometric prompt. Kept framework-agnostic so use cases
 * can react to it without importing AndroidX BiometricPrompt types.
 */
@Immutable
sealed interface BiometricAuthResult {
    data object Success : BiometricAuthResult

    /** User dismissed the prompt or pressed the negative button. */
    data object UserCanceled : BiometricAuthResult

    /** Wrong finger / unrecognised face — the user can try again. */
    data object Failed : BiometricAuthResult

    /**
     * Terminal error surfaced by the OS (lockout, hardware error, no
     * biometrics enrolled, etc.).
     */
    data class Error(val code: Int, val message: String) : BiometricAuthResult
}
