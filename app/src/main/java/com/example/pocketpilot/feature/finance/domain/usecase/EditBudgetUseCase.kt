package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.Budget
import com.example.pocketpilot.feature.finance.domain.repository.BudgetRepository

class EditBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: Budget) {
        repository.upsert(budget)
    }
}
