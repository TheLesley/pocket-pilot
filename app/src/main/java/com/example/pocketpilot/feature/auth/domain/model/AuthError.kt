package com.example.pocketpilot.feature.auth.domain.model

sealed class AuthException(message: String) : Exception(message) {
    data object InvalidCredentials : AuthException("Email or password is incorrect")
    data object EmailAlreadyRegistered : AuthException("An account with that email already exists")
    data object AccountNotFound : AuthException("No account matches that email")
    data object InvalidResetCode : AuthException("Reset code is invalid or expired")
    data object Network : AuthException("Network error. Please try again.")
}
