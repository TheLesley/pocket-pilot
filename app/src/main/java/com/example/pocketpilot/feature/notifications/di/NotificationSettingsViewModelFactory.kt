package com.example.pocketpilot.feature.notifications.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocketpilot.feature.notifications.presentation.NotificationSettingsViewModel

class NotificationSettingsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(NotificationSettingsViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return NotificationSettingsViewModel(
            observePreferences = NotificationsContainer.observePreferences,
            updatePreferences = NotificationsContainer.updatePreferences,
            workScheduler = NotificationsContainer.workScheduler,
            dispatcher = NotificationsContainer.dispatcher
        ) as T
    }
}
