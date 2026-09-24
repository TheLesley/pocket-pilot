package com.example.pocketpilot.feature.dashboard.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

/**
 * Sums every transaction the user has recorded, treating income as positive
 * and expenses as negative. The result is the running "current balance"
 * displayed on the dashboard summary card.
 */
class CalculateBalanceUseCase {
    operator fun invoke(transactions: List<Transaction>): Long = transactions.fold(0L) { acc, tx ->
        when (tx.type) {
            TransactionType.INCOME -> acc + tx.amountMinorUnits
            TransactionType.EXPENSE -> acc - tx.amountMinorUnits
        }
    }
}
