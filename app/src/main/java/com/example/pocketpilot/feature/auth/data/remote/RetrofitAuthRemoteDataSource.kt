package com.example.pocketpilot.feature.auth.data.remote

import com.example.pocketpilot.core.network.NetworkException
import com.example.pocketpilot.core.network.safeApiCall
import com.example.pocketpilot.feature.auth.data.remote.dto.AuthResponseDto
import com.example.pocketpilot.feature.auth.data.remote.dto.ForgotPasswordRequestDto
import com.example.pocketpilot.feature.auth.data.remote.dto.LoginRequestDto
import com.example.pocketpilot.feature.auth.data.remote.dto.ResetPasswordRequestDto
import com.example.pocketpilot.feature.auth.data.remote.dto.SignUpRequestDto
import com.example.pocketpilot.feature.auth.domain.model.AuthException

/**
 * Retrofit-backed [AuthRemoteDataSource] that funnels every call through
 * [safeApiCall] and translates transport failures into [AuthException]s the
 * domain already understands.
 */
class RetrofitAuthRemoteDataSource(private val api: AuthApi) : AuthRemoteDataSource {

    override suspend fun login(email: String, password: String): AuthResponseDto = execute {
        api.login(LoginRequestDto(email = email, password = password))
    }

    override suspend fun signUp(name: String, email: String, password: String): AuthResponseDto = execute {
        api.signUp(SignUpRequestDto(name = name, email = email, password = password))
    }

    override suspend fun requestPasswordReset(email: String): String = execute {
        api.requestPasswordReset(ForgotPasswordRequestDto(email = email)).resetCode
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): AuthResponseDto = execute {
        api.resetPassword(
            ResetPasswordRequestDto(email = email, code = code, newPassword = newPassword)
        )
    }

    private suspend inline fun <T> execute(crossinline block: suspend () -> T): T = try {
        safeApiCall { block() }
    } catch (e: NetworkException) {
        throw e.toAuthException()
    }

    private fun NetworkException.toAuthException(): AuthException = when (this) {
        is NetworkException.Unauthorized -> AuthException.InvalidCredentials
        is NetworkException.ClientError -> when (code) {
            404 -> AuthException.AccountNotFound
            409 -> AuthException.EmailAlreadyRegistered
            410, 422 -> AuthException.InvalidResetCode
            else -> AuthException.Network
        }
        is NetworkException.ServerError,
        is NetworkException.Timeout,
        is NetworkException.NoConnectivity,
        is NetworkException.Serialization,
        is NetworkException.Unknown -> AuthException.Network
    }
}
