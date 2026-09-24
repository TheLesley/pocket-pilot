package com.example.pocketpilot.feature.settings.domain.usecase

import com.example.pocketpilot.feature.settings.domain.model.ThemeMode
import com.example.pocketpilot.feature.settings.domain.repository.SettingsRepository

class UpdateThemeModeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }
}
