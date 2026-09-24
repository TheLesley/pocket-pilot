package com.example.pocketpilot.feature.auth.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AuthSession {
    data object SignedOut : AuthSession

    @Immutable data class SignedIn(val user: User, val tokens: AuthTokens) : AuthSession
}
