package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.Budget
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

/**
 * Pure domain calculation: given a [Budget] and a set of [Transaction]s, sum
 * up the minor units *spent* against that budget within its active window.
 *
 * A transaction counts toward the budget when all of the following hold:
 * - it is an `EXPENSE`
 * - its currency matches the budget's currency
 * - it occurred inside `[budget.startsAtEpochMillis, budget.endsAtEpochMillis]`
 *   (the upper bound is treated as "open" when the budget has no end date)
 * - it is either explicitly linked to the budget via `budgetId`, OR the budget
 *   is scoped to a category and the transaction matches that category.
 *
 * Category-scoped budgets that opt into "any transaction in this category"
 * behaviour make onboarding easier — users don't need to manually tag each
 * transaction with a `budgetId` for tracking to kick in.
 */
class CalculateBudgetSpentUseCase {

    operator fun invoke(budget: Budget, transactions: List<Transaction>): Long {
        val end = budget.endsAtEpochMillis ?: Long.MAX_VALUE
        var total = 0L
        for (tx in transactions) {
            if (tx.type != TransactionType.EXPENSE) continue
            if (tx.currencyCode != budget.currencyCode) continue
            if (tx.occurredAtEpochMillis < budget.startsAtEpochMillis) continue
            if (tx.occurredAtEpochMillis > end) continue
            val matchesBudget = tx.budgetId == budget.id
            val matchesCategory = budget.categoryId != null && tx.categoryId == budget.categoryId
            if (matchesBudget || matchesCategory) {
                total += tx.amountMinorUnits
            }
        }
        return total
    }
}
