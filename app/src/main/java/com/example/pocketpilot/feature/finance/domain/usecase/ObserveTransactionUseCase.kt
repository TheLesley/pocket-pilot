package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionUseCase(private val repository: TransactionRepository) {
    operator fun invoke(id: String): Flow<Transaction?> = repository.observeById(id)
}
