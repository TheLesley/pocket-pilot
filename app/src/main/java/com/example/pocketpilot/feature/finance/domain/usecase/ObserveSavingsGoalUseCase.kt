package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal
import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow

class ObserveSavingsGoalUseCase(private val repository: SavingsGoalRepository) {
    operator fun invoke(id: String): Flow<SavingsGoal?> = repository.observeById(id)
}
