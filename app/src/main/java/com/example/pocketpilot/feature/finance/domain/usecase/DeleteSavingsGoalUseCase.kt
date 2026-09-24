package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository

class DeleteSavingsGoalUseCase(private val repository: SavingsGoalRepository) {
    suspend operator fun invoke(id: String) {
        repository.delete(id)
    }
}
