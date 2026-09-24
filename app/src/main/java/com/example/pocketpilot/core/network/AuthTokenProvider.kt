package com.example.pocketpilot.core.network

/**
 * Supplies the current bearer access token to the [AuthHeaderInterceptor]
 * without leaking auth-store types into the network module.
 */
fun interface AuthTokenProvider {
    fun currentAccessToken(): String?
}
