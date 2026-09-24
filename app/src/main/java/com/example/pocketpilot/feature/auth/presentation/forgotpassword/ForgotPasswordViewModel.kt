package com.example.pocketpilot.feature.auth.presentation.forgotpassword

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.auth.domain.usecase.RequestPasswordResetUseCase
import com.example.pocketpilot.feature.auth.domain.validation.AuthValidator
import com.example.pocketpilot.feature.auth.presentation.util.toUserMessage
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(private val requestPasswordResetUseCase: RequestPasswordResetUseCase) :
    BaseViewModel<ForgotPasswordState, ForgotPasswordEvent, ForgotPasswordEffect>(
        ForgotPasswordState()
    ) {

    override fun handleEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> setState {
                copy(email = event.value, emailError = null, submitError = null)
            }
            ForgotPasswordEvent.Submit -> submit()
            ForgotPasswordEvent.ContinueToReset -> {
                if (currentState.isSuccess) {
                    sendEffect(ForgotPasswordEffect.NavigateToResetPassword(currentState.email.trim()))
                }
            }
            ForgotPasswordEvent.BackToLogin -> sendEffect(ForgotPasswordEffect.NavigateBackToLogin)
        }
    }

    private fun submit() {
        val s = currentState
        val emailError = AuthValidator.validateEmail(s.email)?.message
        if (emailError != null) {
            setState { copy(emailError = emailError) }
            return
        }
        setState { copy(isSubmitting = true, submitError = null, issuedCode = null) }
        viewModelScope.launch {
            runCatching { requestPasswordResetUseCase(s.email) }
                .onSuccess { code ->
                    setState { copy(isSubmitting = false, issuedCode = code) }
                }
                .onFailure { t ->
                    setState { copy(isSubmitting = false, submitError = t.toUserMessage()) }
                }
        }
    }
}
