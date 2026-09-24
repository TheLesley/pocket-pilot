package com.example.pocketpilot.feature.settings.data.repository

import com.example.pocketpilot.feature.settings.data.local.PreferencesDataSource
import com.example.pocketpilot.feature.settings.domain.model.ThemeMode
import com.example.pocketpilot.feature.settings.domain.model.UserPreferences
import com.example.pocketpilot.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class SettingsRepositoryImpl(private val dataSource: PreferencesDataSource) : SettingsRepository {

    override fun observe(): Flow<UserPreferences> = dataSource.preferencesFlow.distinctUntilChanged()

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataSource.setThemeMode(mode)
    }

    override suspend fun setDefaultCurrencyCode(currencyCode: String) {
        dataSource.setDefaultCurrencyCode(currencyCode)
    }

    override suspend fun setRecurringNotificationsEnabled(enabled: Boolean) {
        dataSource.setRecurringNotificationsEnabled(enabled)
    }

    override suspend fun setBiometricLockEnabled(enabled: Boolean) {
        dataSource.setBiometricLockEnabled(enabled)
    }

    override suspend fun clearAll() {
        dataSource.clearAll()
    }
}
