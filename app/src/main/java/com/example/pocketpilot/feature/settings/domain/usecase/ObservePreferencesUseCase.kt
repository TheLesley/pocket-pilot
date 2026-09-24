package com.example.pocketpilot.feature.settings.domain.usecase

import com.example.pocketpilot.feature.settings.domain.model.UserPreferences
import com.example.pocketpilot.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObservePreferencesUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<UserPreferences> = repository.observe()
}
