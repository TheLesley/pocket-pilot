package com.example.pocketpilot.feature.auth.data.local

import com.example.pocketpilot.feature.auth.domain.model.AuthTokens
import com.example.pocketpilot.feature.auth.domain.repository.AuthTokenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-scoped token store. Serves as the default implementation of
 * [AuthTokenStore] until a persistent backing is wired in (DataStore with
 * a Tink-encrypted serializer, or EncryptedSharedPreferences). Behaviour is
 * flow-based so replacing the impl is transparent to the repository.
 */
class InMemoryAuthTokenStore : AuthTokenStore {

    private val _tokens = MutableStateFlow<AuthTokens?>(null)

    override val tokens: Flow<AuthTokens?> = _tokens.asStateFlow()

    override suspend fun save(tokens: AuthTokens) {
        _tokens.value = tokens
    }

    override suspend fun clear() {
        _tokens.value = null
    }

    override suspend fun current(): AuthTokens? = _tokens.value
}
