package com.example.pocketpilot.feature.auth.data.remote

import com.example.pocketpilot.feature.auth.data.remote.dto.AuthResponseDto

/**
 * Abstraction over the remote auth API. A Retrofit-backed implementation
 * will replace [FakeAuthRemoteDataSource] once a real backend is wired in.
 */
interface AuthRemoteDataSource {

    suspend fun login(email: String, password: String): AuthResponseDto

    suspend fun signUp(name: String, email: String, password: String): AuthResponseDto

    /** Returns a reset code the caller can present to the user in development builds. */
    suspend fun requestPasswordReset(email: String): String

    suspend fun resetPassword(email: String, code: String, newPassword: String): AuthResponseDto
}
