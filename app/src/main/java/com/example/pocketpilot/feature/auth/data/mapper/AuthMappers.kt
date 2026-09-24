package com.example.pocketpilot.feature.auth.data.mapper

import com.example.pocketpilot.feature.auth.data.remote.dto.AuthTokensDto
import com.example.pocketpilot.feature.auth.data.remote.dto.UserDto
import com.example.pocketpilot.feature.auth.domain.model.AuthTokens
import com.example.pocketpilot.feature.auth.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName
)

fun AuthTokensDto.toDomain(nowMillis: Long): AuthTokens = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresAtEpochMillis = nowMillis + expiresInSeconds * 1_000L
)
