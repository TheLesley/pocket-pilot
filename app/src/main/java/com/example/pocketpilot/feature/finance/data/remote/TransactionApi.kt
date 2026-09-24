package com.example.pocketpilot.feature.finance.data.remote

import com.example.pocketpilot.feature.finance.data.remote.dto.CreateTransactionRequestDto
import com.example.pocketpilot.feature.finance.data.remote.dto.TransactionDto
import com.example.pocketpilot.feature.finance.data.remote.dto.TransactionListResponseDto
import com.example.pocketpilot.feature.finance.data.remote.dto.UpdateTransactionRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TransactionApi {

    @GET("v1/transactions")
    suspend fun list(
        @Query("from") fromEpochMillis: Long? = null,
        @Query("to") toEpochMillis: Long? = null,
        @Query("budget_id") budgetId: String? = null,
        @Query("category_id") categoryId: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null
    ): TransactionListResponseDto

    @GET("v1/transactions/{id}")
    suspend fun get(@Path("id") id: String): TransactionDto

    @POST("v1/transactions")
    suspend fun create(@Body body: CreateTransactionRequestDto): TransactionDto

    @PATCH("v1/transactions/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateTransactionRequestDto): TransactionDto

    @DELETE("v1/transactions/{id}")
    suspend fun delete(@Path("id") id: String)
}
