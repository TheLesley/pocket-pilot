package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal
import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository

class GetSavingsGoalUseCase(private val repository: SavingsGoalRepository) {
    suspend operator fun invoke(id: String): SavingsGoal? = repository.getById(id)
}
