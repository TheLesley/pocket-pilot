package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal
import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository

class AddSavingsGoalUseCase(private val repository: SavingsGoalRepository) {
    suspend operator fun invoke(goal: SavingsGoal) {
        repository.upsert(goal)
    }
}
