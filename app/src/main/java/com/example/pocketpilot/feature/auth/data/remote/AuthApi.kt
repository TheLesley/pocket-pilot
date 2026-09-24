package com.example.pocketpilot.feature.auth.data.remote

import com.example.pocketpilot.feature.auth.data.remote.dto.AuthResponseDto
import com.example.pocketpilot.feature.auth.data.remote.dto.ForgotPasswordRequestDto
import com.example.pocketpilot.feature.auth.data.remote.dto.ForgotPasswordResponseDto
import com.example.pocketpilot.feature.auth.data.remote.dto.LoginRequestDto
import com.example.pocketpilot.feature.auth.data.remote.dto.ResetPasswordRequestDto
import com.example.pocketpilot.feature.auth.data.remote.dto.SignUpRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("v1/auth/signup")
    suspend fun signUp(@Body body: SignUpRequestDto): AuthResponseDto

    @POST("v1/auth/password/forgot")
    suspend fun requestPasswordReset(@Body body: ForgotPasswordRequestDto): ForgotPasswordResponseDto

    @POST("v1/auth/password/reset")
    suspend fun resetPassword(@Body body: ResetPasswordRequestDto): AuthResponseDto
}
