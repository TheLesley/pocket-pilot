package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.Budget
import com.example.pocketpilot.feature.finance.domain.repository.BudgetRepository

class GetBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(id: String): Budget? = repository.getById(id)
}
