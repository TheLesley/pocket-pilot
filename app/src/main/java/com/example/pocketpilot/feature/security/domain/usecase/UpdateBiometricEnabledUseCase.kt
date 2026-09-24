package com.example.pocketpilot.feature.security.domain.usecase

import com.example.pocketpilot.feature.security.domain.repository.SecurityRepository

class UpdateBiometricEnabledUseCase(private val repository: SecurityRepository) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setBiometricEnabled(enabled)
    }
}
