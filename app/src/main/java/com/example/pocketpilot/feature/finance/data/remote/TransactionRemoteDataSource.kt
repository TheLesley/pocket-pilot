package com.example.pocketpilot.feature.finance.data.remote

import com.example.pocketpilot.core.network.safeApiCall
import com.example.pocketpilot.feature.finance.data.mapper.toCreateRequestDto
import com.example.pocketpilot.feature.finance.data.mapper.toDomain
import com.example.pocketpilot.feature.finance.data.mapper.toUpdateRequestDto
import com.example.pocketpilot.feature.finance.domain.model.Transaction

/**
 * Boundary between the transaction repository and the network stack. Returns
 * pure domain models — DTOs never escape this package.
 */
interface TransactionRemoteDataSource {

    suspend fun list(
        fromEpochMillis: Long? = null,
        toEpochMillis: Long? = null,
        budgetId: String? = null,
        categoryId: String? = null,
        cursor: String? = null,
        limit: Int? = null
    ): TransactionPage

    suspend fun get(id: String): Transaction

    suspend fun create(transaction: Transaction): Transaction

    suspend fun update(transaction: Transaction): Transaction

    suspend fun delete(id: String)
}

data class TransactionPage(val items: List<Transaction>, val nextCursor: String?)

class RetrofitTransactionRemoteDataSource(private val api: TransactionApi) : TransactionRemoteDataSource {

    override suspend fun list(
        fromEpochMillis: Long?,
        toEpochMillis: Long?,
        budgetId: String?,
        categoryId: String?,
        cursor: String?,
        limit: Int?
    ): TransactionPage = safeApiCall {
        val response = api.list(
            fromEpochMillis = fromEpochMillis,
            toEpochMillis = toEpochMillis,
            budgetId = budgetId,
            categoryId = categoryId,
            cursor = cursor,
            limit = limit
        )
        TransactionPage(
            items = response.items.map { it.toDomain() },
            nextCursor = response.nextCursor
        )
    }

    override suspend fun get(id: String): Transaction = safeApiCall {
        api.get(id).toDomain()
    }

    override suspend fun create(transaction: Transaction): Transaction = safeApiCall {
        api.create(transaction.toCreateRequestDto()).toDomain()
    }

    override suspend fun update(transaction: Transaction): Transaction = safeApiCall {
        api.update(transaction.id, transaction.toUpdateRequestDto()).toDomain()
    }

    override suspend fun delete(id: String): Unit = safeApiCall {
        api.delete(id)
    }
}
