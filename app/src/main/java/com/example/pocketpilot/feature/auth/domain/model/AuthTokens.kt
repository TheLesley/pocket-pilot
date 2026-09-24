package com.example.pocketpilot.feature.auth.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class AuthTokens(val accessToken: String, val refreshToken: String, val expiresAtEpochMillis: Long)
