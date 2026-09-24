package com.example.pocketpilot.feature.finance.domain.repository

import com.example.pocketpilot.feature.finance.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {

    fun observeAll(): Flow<List<Budget>>

    fun observeById(id: String): Flow<Budget?>

    suspend fun getById(id: String): Budget?

    suspend fun upsert(budget: Budget)

    suspend fun delete(id: String)
}
