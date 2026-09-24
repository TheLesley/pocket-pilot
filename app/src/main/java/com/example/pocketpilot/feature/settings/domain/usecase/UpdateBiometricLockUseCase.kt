package com.example.pocketpilot.feature.settings.domain.usecase

import com.example.pocketpilot.feature.settings.domain.repository.SettingsRepository

class UpdateBiometricLockUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setBiometricLockEnabled(enabled)
    }
}
