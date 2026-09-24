package com.example.pocketpilot.feature.security.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pocketpilot.feature.security.data.mapper.toDomain
import com.example.pocketpilot.feature.security.domain.model.AutoLockTimeout
import com.example.pocketpilot.feature.security.domain.model.SecurityPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore-backed persistence for security preferences. Kept in its own file
 * (separate from the general settings DataStore) so the PIN hash lives in a
 * dedicated store that can be independently cleared or migrated to
 * EncryptedSharedPreferences without touching unrelated preferences.
 */
class SecurityPreferencesDataSource(private val dataStore: DataStore<Preferences>) {

    val preferencesFlow: Flow<SecurityPreferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { it.toDomain() }

    suspend fun current(): SecurityPreferences = preferencesFlow.first()

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BiometricEnabled] = enabled }
    }

    suspend fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        dataStore.edit { it[Keys.AutoLockTimeout] = timeout.name }
    }

    suspend fun setPin(hash: String, salt: String) {
        dataStore.edit {
            it[Keys.PinHash] = hash
            it[Keys.PinSalt] = salt
        }
    }

    suspend fun clearPin() {
        dataStore.edit {
            it.remove(Keys.PinHash)
            it.remove(Keys.PinSalt)
        }
    }

    internal object Keys {
        val BiometricEnabled = booleanPreferencesKey("biometric_enabled")
        val AutoLockTimeout = stringPreferencesKey("auto_lock_timeout")
        val PinHash = stringPreferencesKey("pin_hash")
        val PinSalt = stringPreferencesKey("pin_salt")
    }

    companion object {
        private const val DATASTORE_NAME = "pocketpilot_security_preferences"

        private val Context.securityDataStore by preferencesDataStore(name = DATASTORE_NAME)

        fun create(context: Context): SecurityPreferencesDataSource =
            SecurityPreferencesDataSource(context.applicationContext.securityDataStore)
    }
}
