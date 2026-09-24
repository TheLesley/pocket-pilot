package com.example.pocketpilot.feature.analytics.domain.usecase

import com.example.pocketpilot.feature.analytics.domain.model.CategoryDistribution
import com.example.pocketpilot.feature.analytics.domain.model.CategoryShare
import com.example.pocketpilot.feature.analytics.domain.model.DateRange
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

/**
 * Groups the expense transactions inside [range] by `categoryId` and returns a
 * chart-ready [CategoryDistribution]. Uncategorised entries collapse into a
 * single "Uncategorised" bucket so they still appear on the donut instead of
 * being silently dropped.
 */
class CalculateCategoryDistributionUseCase {

    operator fun invoke(transactions: List<Transaction>, range: DateRange): CategoryDistribution {
        val totals = LinkedHashMap<String?, Long>()
        val counts = LinkedHashMap<String?, Int>()
        var overall = 0L
        for (tx in transactions) {
            if (tx.type != TransactionType.EXPENSE) continue
            if (tx.occurredAtEpochMillis !in range) continue
            val key = tx.categoryId?.takeIf { it.isNotBlank() }
            totals[key] = (totals[key] ?: 0L) + tx.amountMinorUnits
            counts[key] = (counts[key] ?: 0) + 1
            overall += tx.amountMinorUnits
        }
        if (overall == 0L) {
            return CategoryDistribution(
                range = range,
                totalExpenseMinorUnits = 0L,
                shares = emptyList()
            )
        }
        val shares = totals.entries
            .sortedByDescending { it.value }
            .map { (categoryId, amount) ->
                CategoryShare(
                    categoryId = categoryId,
                    label = categoryId ?: UNCATEGORISED_LABEL,
                    amountMinorUnits = amount,
                    share = (amount.toDouble() / overall.toDouble()).toFloat(),
                    transactionCount = counts[categoryId] ?: 0
                )
            }
        return CategoryDistribution(
            range = range,
            totalExpenseMinorUnits = overall,
            shares = shares
        )
    }

    private companion object {
        const val UNCATEGORISED_LABEL = "Uncategorised"
    }
}
