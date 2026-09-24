package com.example.pocketpilot.feature.auth.presentation.resetpassword

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent

@Immutable
data class ResetPasswordState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val codeError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
            code.isNotBlank() &&
            newPassword.isNotBlank() &&
            confirmPassword.isNotBlank()
}

sealed interface ResetPasswordEvent : UiEvent {
    data class CodeChanged(val value: String) : ResetPasswordEvent
    data class PasswordChanged(val value: String) : ResetPasswordEvent
    data class ConfirmPasswordChanged(val value: String) : ResetPasswordEvent
    data object Submit : ResetPasswordEvent
    data object BackToLogin : ResetPasswordEvent
}

sealed interface ResetPasswordEffect : UiEffect {
    data object NavigateToLogin : ResetPasswordEffect
    data object NavigateToHome : ResetPasswordEffect
}
