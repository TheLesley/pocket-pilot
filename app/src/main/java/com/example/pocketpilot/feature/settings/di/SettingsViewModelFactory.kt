package com.example.pocketpilot.feature.settings.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocketpilot.feature.settings.presentation.SettingsViewModel

class SettingsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return SettingsViewModel(
            observePreferences = SettingsContainer.observePreferences,
            updateThemeMode = SettingsContainer.updateThemeMode,
            updateDefaultCurrency = SettingsContainer.updateDefaultCurrency,
            updateRecurringNotifications = SettingsContainer.updateRecurringNotifications,
            updateBiometricLock = SettingsContainer.updateBiometricLock,
            clearPreferences = SettingsContainer.clearPreferences
        ) as T
    }
}
