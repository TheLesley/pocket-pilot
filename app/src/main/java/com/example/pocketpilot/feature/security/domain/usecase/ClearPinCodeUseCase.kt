package com.example.pocketpilot.feature.security.domain.usecase

import com.example.pocketpilot.feature.security.domain.repository.SecurityRepository

class ClearPinCodeUseCase(private val repository: SecurityRepository) {
    suspend operator fun invoke() {
        repository.clearPin()
    }
}
