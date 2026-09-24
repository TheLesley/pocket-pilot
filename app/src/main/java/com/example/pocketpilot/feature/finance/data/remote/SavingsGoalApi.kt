package com.example.pocketpilot.feature.finance.data.remote

import com.example.pocketpilot.feature.finance.data.remote.dto.AdjustSavingsContributionRequestDto
import com.example.pocketpilot.feature.finance.data.remote.dto.CreateSavingsGoalRequestDto
import com.example.pocketpilot.feature.finance.data.remote.dto.SavingsGoalDto
import com.example.pocketpilot.feature.finance.data.remote.dto.SavingsGoalListResponseDto
import com.example.pocketpilot.feature.finance.data.remote.dto.UpdateSavingsGoalRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface SavingsGoalApi {

    @GET("v1/savings-goals")
    suspend fun list(): SavingsGoalListResponseDto

    @GET("v1/savings-goals/{id}")
    suspend fun get(@Path("id") id: String): SavingsGoalDto

    @POST("v1/savings-goals")
    suspend fun create(@Body body: CreateSavingsGoalRequestDto): SavingsGoalDto

    @PATCH("v1/savings-goals/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateSavingsGoalRequestDto): SavingsGoalDto

    @POST("v1/savings-goals/{id}/contributions")
    suspend fun adjustContribution(@Path("id") id: String, @Body body: AdjustSavingsContributionRequestDto): SavingsGoalDto

    @DELETE("v1/savings-goals/{id}")
    suspend fun delete(@Path("id") id: String)
}
