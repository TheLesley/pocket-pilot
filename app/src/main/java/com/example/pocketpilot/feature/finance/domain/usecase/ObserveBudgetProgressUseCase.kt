package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.BudgetProgress
import com.example.pocketpilot.feature.finance.domain.repository.BudgetRepository
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Emits the live [BudgetProgress] for a single budget id. Emits `null` while
 * the budget is unknown (deleted or never existed) so the detail screen can
 * render an empty state.
 */
class ObserveBudgetProgressUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val calculateBudgetSpent: CalculateBudgetSpentUseCase
) {
    operator fun invoke(id: String): Flow<BudgetProgress?> = combine(
        budgetRepository.observeById(id),
        transactionRepository.observeAll()
    ) { budget, transactions ->
        budget?.let {
            BudgetProgress(
                budget = it,
                spentMinorUnits = calculateBudgetSpent(it, transactions)
            )
        }
    }
}
