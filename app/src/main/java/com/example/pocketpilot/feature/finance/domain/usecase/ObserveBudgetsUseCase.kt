package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.Budget
import com.example.pocketpilot.feature.finance.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

class ObserveBudgetsUseCase(private val repository: BudgetRepository) {
    operator fun invoke(): Flow<List<Budget>> = repository.observeAll()
}
