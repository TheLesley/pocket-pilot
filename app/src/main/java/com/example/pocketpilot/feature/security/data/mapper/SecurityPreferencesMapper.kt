package com.example.pocketpilot.feature.security.data.mapper

import androidx.datastore.preferences.core.Preferences
import com.example.pocketpilot.feature.security.data.local.SecurityPreferencesDataSource
import com.example.pocketpilot.feature.security.domain.model.AutoLockTimeout
import com.example.pocketpilot.feature.security.domain.model.SecurityPreferences

internal fun Preferences.toDomain(): SecurityPreferences = SecurityPreferences(
    biometricEnabled = this[SecurityPreferencesDataSource.Keys.BiometricEnabled] ?: false,
    autoLockTimeout = AutoLockTimeout.fromNameOrDefault(
        this[SecurityPreferencesDataSource.Keys.AutoLockTimeout]
    ),
    pinHash = this[SecurityPreferencesDataSource.Keys.PinHash].orEmpty(),
    pinSalt = this[SecurityPreferencesDataSource.Keys.PinSalt].orEmpty()
)
