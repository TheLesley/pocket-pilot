package com.example.pocketpilot.feature.dashboard.domain.usecase

import com.example.pocketpilot.feature.dashboard.domain.model.CategorySpend
import com.example.pocketpilot.feature.dashboard.domain.model.MonthRange
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

/**
 * Groups expense transactions inside [range] by their `categoryId` and returns
 * the sorted list of category spends, largest first. Uncategorised expenses
 * collapse into a single "Uncategorised" bucket so they still appear in the
 * breakdown chart instead of being silently dropped.
 */
class CalculateSpendingByCategoryUseCase {

    operator fun invoke(transactions: List<Transaction>, range: MonthRange): List<CategorySpend> {
        val totals = LinkedHashMap<String?, Long>()
        for (tx in transactions) {
            if (tx.type != TransactionType.EXPENSE) continue
            if (tx.occurredAtEpochMillis !in range) continue
            val key = tx.categoryId?.takeIf { it.isNotBlank() }
            totals[key] = (totals[key] ?: 0L) + tx.amountMinorUnits
        }
        val overall = totals.values.sum()
        if (overall == 0L) return emptyList()
        return totals.entries
            .sortedByDescending { it.value }
            .map { (categoryId, amount) ->
                CategorySpend(
                    categoryId = categoryId,
                    label = categoryId ?: UNCATEGORISED_LABEL,
                    amountMinorUnits = amount,
                    share = (amount.toDouble() / overall.toDouble()).toFloat()
                )
            }
    }

    private companion object {
        const val UNCATEGORISED_LABEL = "Uncategorised"
    }
}
