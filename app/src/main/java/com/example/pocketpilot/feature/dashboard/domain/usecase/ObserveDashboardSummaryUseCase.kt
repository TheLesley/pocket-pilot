package com.example.pocketpilot.feature.dashboard.domain.usecase

import com.example.pocketpilot.feature.dashboard.domain.model.DashboardSummary
import com.example.pocketpilot.feature.dashboard.domain.model.MonthRange
import com.example.pocketpilot.feature.finance.domain.repository.BudgetRepository
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Orchestrator use case that observes every persisted transaction plus every
 * budget and reduces them into a single [DashboardSummary]. Combining the
 * flows at the domain boundary means the ViewModel receives one atomic
 * snapshot per emission instead of interleaving partial updates.
 *
 * Defaults to [DEFAULT_CURRENCY] when no transactions exist yet — the very
 * first user opening the app should still see a coherent, zeroed summary
 * rather than a loading spinner that never clears.
 */
class ObserveDashboardSummaryUseCase(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val calculateBalance: CalculateBalanceUseCase = CalculateBalanceUseCase(),
    private val calculateMonthlyTotals: CalculateMonthlyTotalsUseCase = CalculateMonthlyTotalsUseCase(),
    private val calculateMonthlyBudget: CalculateMonthlyBudgetUseCase = CalculateMonthlyBudgetUseCase(),
    private val calculateSpendingByCategory: CalculateSpendingByCategoryUseCase = CalculateSpendingByCategoryUseCase(),
    private val clock: () -> Long = { System.currentTimeMillis() }
) {

    operator fun invoke(recentLimit: Int = DEFAULT_RECENT_LIMIT): Flow<DashboardSummary> = combine(
        transactionRepository.observeAll(),
        budgetRepository.observeAll()
    ) { transactions, budgets ->
        val range = MonthRange.containing(clock())
        val totals = calculateMonthlyTotals(transactions, range)
        val currency = transactions.firstOrNull()?.currencyCode
            ?: budgets.firstOrNull()?.currencyCode
            ?: DEFAULT_CURRENCY
        DashboardSummary(
            currencyCode = currency,
            currentBalanceMinorUnits = calculateBalance(transactions),
            monthlyIncomeMinorUnits = totals.incomeMinorUnits,
            monthlyExpenseMinorUnits = totals.expenseMinorUnits,
            monthlyBudgetMinorUnits = calculateMonthlyBudget(budgets, range),
            categoryBreakdown = calculateSpendingByCategory(transactions, range),
            recentTransactions = transactions
                .sortedByDescending { it.occurredAtEpochMillis }
                .take(recentLimit),
            monthRange = range
        )
    }

    private companion object {
        const val DEFAULT_RECENT_LIMIT = 5
        const val DEFAULT_CURRENCY = "USD"
    }
}
