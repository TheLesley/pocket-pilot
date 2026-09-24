package com.example.pocketpilot.feature.security.domain.usecase

import com.example.pocketpilot.feature.security.domain.model.SecurityPreferences
import com.example.pocketpilot.feature.security.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow

class ObserveSecurityPreferencesUseCase(private val repository: SecurityRepository) {
    operator fun invoke(): Flow<SecurityPreferences> = repository.observe()
}
