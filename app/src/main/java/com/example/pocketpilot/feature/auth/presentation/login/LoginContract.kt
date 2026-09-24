package com.example.pocketpilot.feature.auth.presentation.login

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent

@Immutable
data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null
) {
    val canSubmit: Boolean
        get() = !isSubmitting && email.isNotBlank() && password.isNotBlank()
}

sealed interface LoginEvent : UiEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object Submit : LoginEvent
    data object ForgotPasswordClicked : LoginEvent
    data object SignUpClicked : LoginEvent
}

sealed interface LoginEffect : UiEffect {
    data object NavigateToHome : LoginEffect
    data object NavigateToForgotPassword : LoginEffect
    data object NavigateToSignUp : LoginEffect
}
