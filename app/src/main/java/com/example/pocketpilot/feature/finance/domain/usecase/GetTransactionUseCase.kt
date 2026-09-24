package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository

class GetTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(id: String): Transaction? = repository.getById(id)
}
