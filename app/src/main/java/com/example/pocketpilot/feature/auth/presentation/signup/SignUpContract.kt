package com.example.pocketpilot.feature.auth.presentation.signup

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent

@Immutable
data class SignUpState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
            name.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank()
}

sealed interface SignUpEvent : UiEvent {
    data class NameChanged(val value: String) : SignUpEvent
    data class EmailChanged(val value: String) : SignUpEvent
    data class PasswordChanged(val value: String) : SignUpEvent
    data class ConfirmPasswordChanged(val value: String) : SignUpEvent
    data object Submit : SignUpEvent
    data object LoginClicked : SignUpEvent
}

sealed interface SignUpEffect : UiEffect {
    data object NavigateToHome : SignUpEffect
    data object NavigateToLogin : SignUpEffect
}
