package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository

class DeleteTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(id: String) {
        repository.delete(id)
    }
}
