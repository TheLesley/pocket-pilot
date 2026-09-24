package com.example.pocketpilot.feature.auth.domain.usecase

import com.example.pocketpilot.feature.auth.domain.model.User
import com.example.pocketpilot.feature.auth.domain.repository.AuthRepository

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(name: String, email: String, password: String): User =
        repository.signUp(name.trim(), email.trim(), password)
}
