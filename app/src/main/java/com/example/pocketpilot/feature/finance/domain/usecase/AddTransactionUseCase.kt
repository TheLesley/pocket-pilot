package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository

/**
 * Persists a brand-new transaction. Callers are expected to have validated
 * user input via `TransactionValidator` and mapped it into a fully-formed
 * `Transaction` domain object; this use case is intentionally a thin,
 * single-purpose boundary over the repository so that the ViewModel does not
 * couple to persistence details.
 */
class AddTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.upsert(transaction)
    }
}
