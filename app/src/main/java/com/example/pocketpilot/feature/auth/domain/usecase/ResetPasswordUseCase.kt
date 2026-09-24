package com.example.pocketpilot.feature.auth.domain.usecase

import com.example.pocketpilot.feature.auth.domain.repository.AuthRepository

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, code: String, newPassword: String) {
        repository.resetPassword(email.trim(), code.trim(), newPassword)
    }
}
