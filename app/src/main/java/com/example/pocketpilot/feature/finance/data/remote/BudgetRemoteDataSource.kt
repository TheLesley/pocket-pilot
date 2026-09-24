package com.example.pocketpilot.feature.finance.data.remote

import com.example.pocketpilot.core.network.safeApiCall
import com.example.pocketpilot.feature.finance.data.mapper.toCreateRequestDto
import com.example.pocketpilot.feature.finance.data.mapper.toDomain
import com.example.pocketpilot.feature.finance.data.mapper.toUpdateRequestDto
import com.example.pocketpilot.feature.finance.domain.model.Budget

interface BudgetRemoteDataSource {
    suspend fun list(): List<Budget>
    suspend fun get(id: String): Budget
    suspend fun create(budget: Budget): Budget
    suspend fun update(budget: Budget): Budget
    suspend fun delete(id: String)
}

class RetrofitBudgetRemoteDataSource(private val api: BudgetApi) : BudgetRemoteDataSource {

    override suspend fun list(): List<Budget> = safeApiCall {
        api.list().items.map { it.toDomain() }
    }

    override suspend fun get(id: String): Budget = safeApiCall {
        api.get(id).toDomain()
    }

    override suspend fun create(budget: Budget): Budget = safeApiCall {
        api.create(budget.toCreateRequestDto()).toDomain()
    }

    override suspend fun update(budget: Budget): Budget = safeApiCall {
        api.update(budget.id, budget.toUpdateRequestDto()).toDomain()
    }

    override suspend fun delete(id: String): Unit = safeApiCall {
        api.delete(id)
    }
}
