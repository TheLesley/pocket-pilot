package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal
import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow

class ObserveSavingsGoalsUseCase(private val repository: SavingsGoalRepository) {
    operator fun invoke(): Flow<List<SavingsGoal>> = repository.observeAll()
}
