package com.example.pocketpilot.feature.auth.domain.repository

import com.example.pocketpilot.feature.auth.domain.model.AuthTokens
import kotlinx.coroutines.flow.Flow

/**
 * Persistence contract for auth tokens. Kept as an abstraction so the
 * backing store can be swapped for encrypted DataStore or
 * EncryptedSharedPreferences without touching the repository.
 */
interface AuthTokenStore {

    val tokens: Flow<AuthTokens?>

    suspend fun save(tokens: AuthTokens)

    suspend fun clear()

    suspend fun current(): AuthTokens?
}
