package com.example.pocketpilot.feature.auth.presentation.util

import com.example.pocketpilot.feature.auth.domain.model.AuthException

/** Central place to turn thrown auth errors into user-facing copy. */
fun Throwable.toUserMessage(): String = when (this) {
    is AuthException -> message ?: "Authentication error"
    else -> message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
}
