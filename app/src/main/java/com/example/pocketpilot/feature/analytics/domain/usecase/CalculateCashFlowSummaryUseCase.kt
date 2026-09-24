package com.example.pocketpilot.feature.analytics.domain.usecase

import com.example.pocketpilot.feature.analytics.domain.model.CashFlowSummary
import com.example.pocketpilot.feature.analytics.domain.model.DateRange
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

/**
 * Filters transactions to those inside [range] and separates the totals into
 * income and expense buckets. Pure so it can be unit-tested and reused across
 * export / widget flows without an active Flow.
 */
class CalculateCashFlowSummaryUseCase {

    operator fun invoke(transactions: List<Transaction>, range: DateRange, currencyCode: String): CashFlowSummary {
        var income = 0L
        var expense = 0L
        var count = 0
        for (tx in transactions) {
            if (tx.occurredAtEpochMillis !in range) continue
            count += 1
            when (tx.type) {
                TransactionType.INCOME -> income += tx.amountMinorUnits
                TransactionType.EXPENSE -> expense += tx.amountMinorUnits
            }
        }
        return CashFlowSummary(
            currencyCode = currencyCode,
            range = range,
            incomeMinorUnits = income,
            expenseMinorUnits = expense,
            transactionCount = count
        )
    }
}
