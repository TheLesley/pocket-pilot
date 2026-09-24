package com.example.pocketpilot.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches a bearer access token to outgoing requests when one is available.
 * Requests already carrying an Authorization header are left untouched so
 * unauthenticated endpoints (login, sign-up) can opt out.
 */
class AuthHeaderInterceptor(private val tokenProvider: AuthTokenProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.header(HEADER_AUTHORIZATION) != null) {
            return chain.proceed(original)
        }
        val token = tokenProvider.currentAccessToken()
        val request = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder()
                .header(HEADER_AUTHORIZATION, "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
    }
}
