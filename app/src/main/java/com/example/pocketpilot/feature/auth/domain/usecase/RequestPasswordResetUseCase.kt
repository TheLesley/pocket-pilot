package com.example.pocketpilot.feature.auth.domain.usecase

import com.example.pocketpilot.feature.auth.domain.repository.AuthRepository

class RequestPasswordResetUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): String = repository.requestPasswordReset(email.trim())
}
