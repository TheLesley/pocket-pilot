package com.example.pocketpilot.feature.finance.domain.repository

import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionQuery
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun observeAll(): Flow<List<Transaction>>

    fun observe(query: TransactionQuery): Flow<List<Transaction>>

    fun observeByBudget(budgetId: String): Flow<List<Transaction>>

    fun observeInRange(fromEpochMillis: Long, toEpochMillis: Long): Flow<List<Transaction>>

    fun observeById(id: String): Flow<Transaction?>

    suspend fun getById(id: String): Transaction?

    suspend fun upsert(transaction: Transaction)

    suspend fun delete(id: String)
}
