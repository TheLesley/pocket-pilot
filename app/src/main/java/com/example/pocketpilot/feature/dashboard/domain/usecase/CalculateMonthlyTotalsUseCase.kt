package com.example.pocketpilot.feature.dashboard.domain.usecase

import com.example.pocketpilot.feature.dashboard.domain.model.MonthRange
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

data class MonthlyTotals(val incomeMinorUnits: Long, val expenseMinorUnits: Long)

/**
 * Filters transactions to those that occurred inside [range] and returns the
 * separate totals for income and expenses. Kept as a pure function so it can
 * be reused by widgets, tests, or export flows without depending on Flow.
 */
class CalculateMonthlyTotalsUseCase {
    operator fun invoke(transactions: List<Transaction>, range: MonthRange): MonthlyTotals {
        var income = 0L
        var expense = 0L
        for (tx in transactions) {
            if (tx.occurredAtEpochMillis !in range) continue
            when (tx.type) {
                TransactionType.INCOME -> income += tx.amountMinorUnits
                TransactionType.EXPENSE -> expense += tx.amountMinorUnits
            }
        }
        return MonthlyTotals(incomeMinorUnits = income, expenseMinorUnits = expense)
    }
}
