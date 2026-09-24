package com.example.pocketpilot.feature.security.domain.model

import androidx.compose.runtime.Immutable

/**
 * Immutable snapshot of the security preferences the app persists. Kept in one
 * value type so callers can observe a single atomic stream instead of juggling
 * one flow per key.
 *
 * [pinHash] is stored as a hex-encoded SHA-256 digest of [pinSalt] + PIN. An
 * empty [pinHash] means "no PIN configured yet" and forces the lock screen to
 * degrade to biometric-only or an unlocked passthrough.
 */
@Immutable
data class SecurityPreferences(
    val biometricEnabled: Boolean = false,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.Default,
    val pinHash: String = "",
    val pinSalt: String = ""
) {
    val hasPin: Boolean get() = pinHash.isNotEmpty() && pinSalt.isNotEmpty()

    val isLockEnabled: Boolean get() = biometricEnabled || hasPin

    companion object {
        val Default: SecurityPreferences = SecurityPreferences()
    }
}
