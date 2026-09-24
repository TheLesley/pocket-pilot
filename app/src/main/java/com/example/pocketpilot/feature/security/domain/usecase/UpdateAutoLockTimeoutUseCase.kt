package com.example.pocketpilot.feature.security.domain.usecase

import com.example.pocketpilot.feature.security.domain.model.AutoLockTimeout
import com.example.pocketpilot.feature.security.domain.repository.SecurityRepository

class UpdateAutoLockTimeoutUseCase(private val repository: SecurityRepository) {
    suspend operator fun invoke(timeout: AutoLockTimeout) {
        repository.setAutoLockTimeout(timeout)
    }
}
