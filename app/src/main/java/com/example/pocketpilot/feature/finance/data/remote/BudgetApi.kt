package com.example.pocketpilot.feature.finance.data.remote

import com.example.pocketpilot.feature.finance.data.remote.dto.BudgetDto
import com.example.pocketpilot.feature.finance.data.remote.dto.BudgetListResponseDto
import com.example.pocketpilot.feature.finance.data.remote.dto.CreateBudgetRequestDto
import com.example.pocketpilot.feature.finance.data.remote.dto.UpdateBudgetRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface BudgetApi {

    @GET("v1/budgets")
    suspend fun list(): BudgetListResponseDto

    @GET("v1/budgets/{id}")
    suspend fun get(@Path("id") id: String): BudgetDto

    @POST("v1/budgets")
    suspend fun create(@Body body: CreateBudgetRequestDto): BudgetDto

    @PATCH("v1/budgets/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateBudgetRequestDto): BudgetDto

    @DELETE("v1/budgets/{id}")
    suspend fun delete(@Path("id") id: String)
}
