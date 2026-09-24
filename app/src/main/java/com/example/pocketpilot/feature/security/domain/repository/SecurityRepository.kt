package com.example.pocketpilot.feature.security.domain.repository

import com.example.pocketpilot.feature.security.domain.model.AutoLockTimeout
import com.example.pocketpilot.feature.security.domain.model.SecurityPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for security preferences. Follows the same observation-
 * plus-fire-and-forget pattern as [com.example.pocketpilot.feature.settings.
 * domain.repository.SettingsRepository]: mutators return [Unit] and callers
 * observe the resulting state through [observe].
 */
interface SecurityRepository {

    fun observe(): Flow<SecurityPreferences>

    suspend fun current(): SecurityPreferences

    suspend fun setBiometricEnabled(enabled: Boolean)

    suspend fun setAutoLockTimeout(timeout: AutoLockTimeout)

    suspend fun setPin(hash: String, salt: String)

    suspend fun clearPin()
}
