package com.example.pocketpilot.feature.settings.domain.usecase

import com.example.pocketpilot.feature.settings.domain.repository.SettingsRepository

class ClearPreferencesUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke() {
        repository.clearAll()
    }
}
