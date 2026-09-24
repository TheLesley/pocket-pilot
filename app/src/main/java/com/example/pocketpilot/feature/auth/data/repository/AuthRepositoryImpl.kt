package com.example.pocketpilot.feature.auth.data.repository

import com.example.pocketpilot.feature.auth.data.mapper.toDomain
import com.example.pocketpilot.feature.auth.data.remote.AuthRemoteDataSource
import com.example.pocketpilot.feature.auth.data.remote.dto.AuthResponseDto
import com.example.pocketpilot.feature.auth.domain.model.AuthException
import com.example.pocketpilot.feature.auth.domain.model.AuthSession
import com.example.pocketpilot.feature.auth.domain.model.User
import com.example.pocketpilot.feature.auth.domain.repository.AuthRepository
import com.example.pocketpilot.feature.auth.domain.repository.AuthTokenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val tokenStore: AuthTokenStore,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : AuthRepository {

    private val currentUser = MutableStateFlow<User?>(null)

    override val session: Flow<AuthSession> = combine(
        currentUser,
        tokenStore.tokens
    ) { user, tokens ->
        if (user != null && tokens != null) {
            AuthSession.SignedIn(user, tokens)
        } else {
            AuthSession.SignedOut
        }
    }.distinctUntilChanged()

    override suspend fun login(email: String, password: String): User = runAndPersist { remote.login(email, password) }

    override suspend fun signUp(name: String, email: String, password: String): User =
        runAndPersist { remote.signUp(name, email, password) }

    override suspend fun requestPasswordReset(email: String): String = try {
        remote.requestPasswordReset(email)
    } catch (t: Throwable) {
        throw t.asAuthException()
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String) {
        runAndPersist { remote.resetPassword(email, code, newPassword) }
    }

    override suspend fun logout() {
        tokenStore.clear()
        currentUser.value = null
    }

    private suspend fun runAndPersist(block: suspend () -> AuthResponseDto): User = try {
        val response = block()
        val user = response.user.toDomain()
        val tokens = response.tokens.toDomain(clock())
        tokenStore.save(tokens)
        currentUser.value = user
        user
    } catch (t: Throwable) {
        throw t.asAuthException()
    }

    private fun Throwable.asAuthException(): Throwable = if (this is AuthException) this else this
}
