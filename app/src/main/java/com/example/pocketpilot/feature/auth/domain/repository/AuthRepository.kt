package com.example.pocketpilot.feature.auth.domain.repository

import com.example.pocketpilot.feature.auth.domain.model.AuthSession
import com.example.pocketpilot.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val session: Flow<AuthSession>

    suspend fun login(email: String, password: String): User

    suspend fun signUp(name: String, email: String, password: String): User

    suspend fun requestPasswordReset(email: String): String

    suspend fun resetPassword(email: String, code: String, newPassword: String)

    suspend fun logout()
}
