package com.example.pocketpilot.feature.settings.data.mapper

import androidx.datastore.preferences.core.Preferences
import com.example.pocketpilot.feature.settings.data.local.PreferencesDataSource
import com.example.pocketpilot.feature.settings.domain.model.ThemeMode
import com.example.pocketpilot.feature.settings.domain.model.UserPreferences

/**
 * Explicit mapper between the DataStore [Preferences] representation and the
 * [UserPreferences] domain model. An unrecognised persisted [ThemeMode] name
 * falls back to [ThemeMode.SYSTEM] so an old build can never leave the app in
 * an invalid state.
 */
internal fun Preferences.toDomain(): UserPreferences = UserPreferences(
    themeMode = this[PreferencesDataSource.Keys.ThemeMode]
        ?.let { name -> runCatching { ThemeMode.valueOf(name) }.getOrNull() }
        ?: ThemeMode.SYSTEM,
    defaultCurrencyCode = this[PreferencesDataSource.Keys.DefaultCurrencyCode]
        ?: UserPreferences.DEFAULT_CURRENCY_CODE,
    recurringNotificationsEnabled = this[PreferencesDataSource.Keys.RecurringNotificationsEnabled]
        ?: true,
    biometricLockEnabled = this[PreferencesDataSource.Keys.BiometricLockEnabled]
        ?: false
)
