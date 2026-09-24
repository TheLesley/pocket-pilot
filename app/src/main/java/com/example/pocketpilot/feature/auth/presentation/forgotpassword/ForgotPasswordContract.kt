package com.example.pocketpilot.feature.auth.presentation.forgotpassword

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent

@Immutable
data class ForgotPasswordState(
    val email: String = "",
    val emailError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    /** Set once the backend accepts the request. In the fake data source we surface the code. */
    val issuedCode: String? = null
) {
    val canSubmit: Boolean
        get() = !isSubmitting && email.isNotBlank()

    val isSuccess: Boolean
        get() = issuedCode != null
}

sealed interface ForgotPasswordEvent : UiEvent {
    data class EmailChanged(val value: String) : ForgotPasswordEvent
    data object Submit : ForgotPasswordEvent
    data object ContinueToReset : ForgotPasswordEvent
    data object BackToLogin : ForgotPasswordEvent
}

sealed interface ForgotPasswordEffect : UiEffect {
    data class NavigateToResetPassword(val email: String) : ForgotPasswordEffect
    data object NavigateBackToLogin : ForgotPasswordEffect
}
