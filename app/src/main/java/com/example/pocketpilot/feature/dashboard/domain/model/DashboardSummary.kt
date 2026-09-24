package com.example.pocketpilot.feature.dashboard.domain.model

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.feature.finance.domain.model.Transaction

/**
 * Aggregated financial snapshot rendered on the dashboard. Everything is
 * expressed in the account's display currency using minor units so the UI can
 * format with `MoneyFormatter` — no floating-point math bleeds through the
 * domain boundary.
 */
@Immutable
data class DashboardSummary(
    val currencyCode: String,
    val currentBalanceMinorUnits: Long,
    val monthlyIncomeMinorUnits: Long,
    val monthlyExpenseMinorUnits: Long,
    val monthlyBudgetMinorUnits: Long,
    val categoryBreakdown: List<CategorySpend>,
    val recentTransactions: List<Transaction>,
    val monthRange: MonthRange
) {
    val remainingBudgetMinorUnits: Long
        get() = monthlyBudgetMinorUnits - monthlyExpenseMinorUnits

    val hasAnyActivity: Boolean
        get() = recentTransactions.isNotEmpty() ||
            monthlyIncomeMinorUnits != 0L ||
            monthlyExpenseMinorUnits != 0L
}

/**
 * Slice of expense spending grouped by category for the current month.
 * [share] is a value in `[0.0, 1.0]` representing the fraction of the total
 * monthly expense — precomputed so the UI can render bars without doing math.
 */
@Immutable
data class CategorySpend(val categoryId: String?, val label: String, val amountMinorUnits: Long, val share: Float)
