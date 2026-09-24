package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.BudgetProgress
import com.example.pocketpilot.feature.finance.domain.repository.BudgetRepository
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Combines the observable list of budgets with the full transaction stream
 * and folds each budget into a [BudgetProgress] snapshot. Because both inputs
 * are Room-backed [Flow]s, the resulting flow re-emits whenever either the
 * budget definitions or the underlying transactions change.
 */
class ObserveBudgetsProgressUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val calculateBudgetSpent: CalculateBudgetSpentUseCase
) {
    operator fun invoke(): Flow<List<BudgetProgress>> = combine(
        budgetRepository.observeAll(),
        transactionRepository.observeAll()
    ) { budgets, transactions ->
        budgets.map { budget ->
            BudgetProgress(
                budget = budget,
                spentMinorUnits = calculateBudgetSpent(budget, transactions)
            )
        }
    }
}
