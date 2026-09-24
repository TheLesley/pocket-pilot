package com.example.pocketpilot.feature.settings.domain.usecase

import com.example.pocketpilot.feature.settings.domain.repository.SettingsRepository

class UpdateRecurringNotificationsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setRecurringNotificationsEnabled(enabled)
    }
}
