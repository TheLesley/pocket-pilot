package com.example.pocketpilot.feature.auth.domain.usecase

import com.example.pocketpilot.feature.auth.domain.model.AuthSession
import com.example.pocketpilot.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveAuthSessionUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<AuthSession> = repository.session
}
