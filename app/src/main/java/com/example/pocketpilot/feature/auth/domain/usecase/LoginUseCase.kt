package com.example.pocketpilot.feature.auth.domain.usecase

import com.example.pocketpilot.feature.auth.domain.model.User
import com.example.pocketpilot.feature.auth.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): User = repository.login(email.trim(), password)
}
