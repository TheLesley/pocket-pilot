package com.example.pocketpilot.feature.settings.domain.repository

import com.example.pocketpilot.feature.settings.domain.model.ThemeMode
import com.example.pocketpilot.feature.settings.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for user preferences. The observation stream is the
 * single source of truth — mutators are fire-and-forget and callers observe
 * the resulting state through [observe].
 */
interface SettingsRepository {

    fun observe(): Flow<UserPreferences>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setDefaultCurrencyCode(currencyCode: String)

    suspend fun setRecurringNotificationsEnabled(enabled: Boolean)

    suspend fun setBiometricLockEnabled(enabled: Boolean)

    suspend fun clearAll()
}
