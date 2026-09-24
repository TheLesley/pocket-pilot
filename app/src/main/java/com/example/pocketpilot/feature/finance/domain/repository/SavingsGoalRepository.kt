package com.example.pocketpilot.feature.finance.domain.repository

import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface SavingsGoalRepository {

    fun observeAll(): Flow<List<SavingsGoal>>

    fun observeById(id: String): Flow<SavingsGoal?>

    suspend fun getById(id: String): SavingsGoal?

    suspend fun upsert(goal: SavingsGoal)

    suspend fun addContribution(id: String, amountMinorUnits: Long, updatedAtEpochMillis: Long)

    suspend fun delete(id: String)
}
