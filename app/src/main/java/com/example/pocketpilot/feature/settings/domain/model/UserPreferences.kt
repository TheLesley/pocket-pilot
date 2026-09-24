package com.example.pocketpilot.feature.settings.domain.model

import androidx.compose.runtime.Immutable

/**
 * Immutable snapshot of every user preference the app persists. Kept as a
 * single value type so downstream layers can observe one atomic stream instead
 * of juggling one flow per key.
 */
@Immutable
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultCurrencyCode: String = DEFAULT_CURRENCY_CODE,
    val recurringNotificationsEnabled: Boolean = true,
    val biometricLockEnabled: Boolean = false
) {
    companion object {
        const val DEFAULT_CURRENCY_CODE: String = "USD"
        val Default: UserPreferences = UserPreferences()
    }
}
