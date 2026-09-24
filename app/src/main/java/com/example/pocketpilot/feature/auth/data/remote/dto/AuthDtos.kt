package com.example.pocketpilot.feature.auth.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String,
    @SerialName("display_name") val displayName: String? = null
)

@Serializable
data class AuthTokensDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresInSeconds: Long
)

@Serializable
data class AuthResponseDto(@SerialName("user") val user: UserDto, @SerialName("tokens") val tokens: AuthTokensDto)

@Serializable
data class LoginRequestDto(@SerialName("email") val email: String, @SerialName("password") val password: String)

@Serializable
data class SignUpRequestDto(
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class ForgotPasswordRequestDto(@SerialName("email") val email: String)

@Serializable
data class ForgotPasswordResponseDto(@SerialName("reset_code") val resetCode: String)

@Serializable
data class ResetPasswordRequestDto(
    @SerialName("email") val email: String,
    @SerialName("code") val code: String,
    @SerialName("new_password") val newPassword: String
)
