package com.example.pocketpilot.feature.settings.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pocketpilot.feature.settings.data.mapper.toDomain
import com.example.pocketpilot.feature.settings.domain.model.ThemeMode
import com.example.pocketpilot.feature.settings.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Thin wrapper around a Preferences [DataStore] that speaks only in domain
 * types. Isolating DataStore here keeps the repository free of `Preferences`
 * plumbing and lets us swap storage later without touching upstream code.
 *
 * IO errors while reading are downgraded to [emptyPreferences] so a corrupt
 * file cannot crash the whole app on boot.
 */
class PreferencesDataSource(private val dataStore: DataStore<Preferences>) {

    val preferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { it.toDomain() }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.ThemeMode] = mode.name }
    }

    suspend fun setDefaultCurrencyCode(currencyCode: String) {
        dataStore.edit { it[Keys.DefaultCurrencyCode] = currencyCode }
    }

    suspend fun setRecurringNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.RecurringNotificationsEnabled] = enabled }
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BiometricLockEnabled] = enabled }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    internal object Keys {
        val ThemeMode = stringPreferencesKey("theme_mode")
        val DefaultCurrencyCode = stringPreferencesKey("default_currency_code")
        val RecurringNotificationsEnabled = booleanPreferencesKey("recurring_notifications_enabled")
        val BiometricLockEnabled = booleanPreferencesKey("biometric_lock_enabled")
    }

    companion object {
        private const val DATASTORE_NAME = "pocketpilot_user_preferences"

        private val Context.settingsDataStore by preferencesDataStore(name = DATASTORE_NAME)

        fun create(context: Context): PreferencesDataSource = PreferencesDataSource(context.applicationContext.settingsDataStore)
    }
}
