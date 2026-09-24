package com.example.pocketpilot.feature.finance.data.remote

import com.example.pocketpilot.core.network.safeApiCall
import com.example.pocketpilot.feature.finance.data.mapper.toCreateRequestDto
import com.example.pocketpilot.feature.finance.data.mapper.toDomain
import com.example.pocketpilot.feature.finance.data.mapper.toUpdateRequestDto
import com.example.pocketpilot.feature.finance.data.remote.dto.AdjustSavingsContributionRequestDto
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal

interface SavingsGoalRemoteDataSource {
    suspend fun list(): List<SavingsGoal>
    suspend fun get(id: String): SavingsGoal
    suspend fun create(goal: SavingsGoal): SavingsGoal
    suspend fun update(goal: SavingsGoal): SavingsGoal
    suspend fun adjustContribution(id: String, deltaMinorUnits: Long): SavingsGoal
    suspend fun delete(id: String)
}

class RetrofitSavingsGoalRemoteDataSource(private val api: SavingsGoalApi) : SavingsGoalRemoteDataSource {

    override suspend fun list(): List<SavingsGoal> = safeApiCall {
        api.list().items.map { it.toDomain() }
    }

    override suspend fun get(id: String): SavingsGoal = safeApiCall {
        api.get(id).toDomain()
    }

    override suspend fun create(goal: SavingsGoal): SavingsGoal = safeApiCall {
        api.create(goal.toCreateRequestDto()).toDomain()
    }

    override suspend fun update(goal: SavingsGoal): SavingsGoal = safeApiCall {
        api.update(goal.id, goal.toUpdateRequestDto()).toDomain()
    }

    override suspend fun adjustContribution(id: String, deltaMinorUnits: Long): SavingsGoal = safeApiCall {
        api.adjustContribution(
            id = id,
            body = AdjustSavingsContributionRequestDto(deltaMinorUnits = deltaMinorUnits)
        ).toDomain()
    }

    override suspend fun delete(id: String): Unit = safeApiCall {
        api.delete(id)
    }
}
