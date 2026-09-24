package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.repository.BudgetRepository

class DeleteBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(id: String) {
        repository.delete(id)
    }
}
