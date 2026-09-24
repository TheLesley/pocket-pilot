package com.example.pocketpilot.feature.settings.di

import android.content.Context
import com.example.pocketpilot.feature.settings.data.local.PreferencesDataSource
import com.example.pocketpilot.feature.settings.data.repository.SettingsRepositoryImpl
import com.example.pocketpilot.feature.settings.domain.repository.SettingsRepository
import com.example.pocketpilot.feature.settings.domain.usecase.ClearPreferencesUseCase
import com.example.pocketpilot.feature.settings.domain.usecase.ObservePreferencesUseCase
import com.example.pocketpilot.feature.settings.domain.usecase.UpdateBiometricLockUseCase
import com.example.pocketpilot.feature.settings.domain.usecase.UpdateDefaultCurrencyUseCase
import com.example.pocketpilot.feature.settings.domain.usecase.UpdateRecurringNotificationsUseCase
import com.example.pocketpilot.feature.settings.domain.usecase.UpdateThemeModeUseCase

/**
 * Service locator for the settings feature. Wires DataStore → repository →
 * use cases in the same shape as the other feature containers (dashboard,
 * finance) so the eventual Hilt migration is mechanical.
 */
object SettingsContainer {

    @Volatile
    private var dataSource: PreferencesDataSource? = null

    fun init(context: Context) {
        if (dataSource == null) {
            synchronized(this) {
                if (dataSource == null) {
                    dataSource = PreferencesDataSource.create(context)
                }
            }
        }
    }

    private fun requireDataSource(): PreferencesDataSource = checkNotNull(dataSource) {
        "SettingsContainer.init(context) must be called before accessing preferences."
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(requireDataSource())
    }

    val observePreferences: ObservePreferencesUseCase by lazy {
        ObservePreferencesUseCase(settingsRepository)
    }

    val updateThemeMode: UpdateThemeModeUseCase by lazy {
        UpdateThemeModeUseCase(settingsRepository)
    }

    val updateDefaultCurrency: UpdateDefaultCurrencyUseCase by lazy {
        UpdateDefaultCurrencyUseCase(settingsRepository)
    }

    val updateRecurringNotifications: UpdateRecurringNotificationsUseCase by lazy {
        UpdateRecurringNotificationsUseCase(settingsRepository)
    }

    val updateBiometricLock: UpdateBiometricLockUseCase by lazy {
        UpdateBiometricLockUseCase(settingsRepository)
    }

    val clearPreferences: ClearPreferencesUseCase by lazy {
        ClearPreferencesUseCase(settingsRepository)
    }
}
