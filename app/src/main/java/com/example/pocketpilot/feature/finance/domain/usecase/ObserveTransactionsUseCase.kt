package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionQuery
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(query: TransactionQuery = TransactionQuery()): Flow<List<Transaction>> = repository.observe(query)
}
