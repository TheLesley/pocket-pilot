package com.example.pocketpilot.feature.analytics.domain.model

import androidx.compose.runtime.Immutable

/**
 * Aggregate income/expense picture for a [DateRange]. Everything is in minor
 * units so the UI can format with `MoneyFormatter` without doing floating-point
 * math. [savingsRate] is precomputed and clamped to `[-1.0, 1.0]` for chart use.
 */
@Immutable
data class CashFlowSummary(
    val currencyCode: String,
    val range: DateRange,
    val incomeMinorUnits: Long,
    val expenseMinorUnits: Long,
    val transactionCount: Int
) {
    val netMinorUnits: Long get() = incomeMinorUnits - expenseMinorUnits

    val savingsRate: Float
        get() = if (incomeMinorUnits <= 0L) {
            0f
        } else {
            (netMinorUnits.toFloat() / incomeMinorUnits.toFloat()).coerceIn(-1f, 1f)
        }

    val isEmpty: Boolean get() = incomeMinorUnits == 0L && expenseMinorUnits == 0L
}
