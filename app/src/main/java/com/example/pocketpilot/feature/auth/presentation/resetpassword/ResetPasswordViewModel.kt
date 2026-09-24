package com.example.pocketpilot.feature.auth.presentation.resetpassword

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.auth.domain.usecase.ResetPasswordUseCase
import com.example.pocketpilot.feature.auth.domain.validation.AuthValidator
import com.example.pocketpilot.feature.auth.presentation.util.toUserMessage
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val resetPasswordUseCase: ResetPasswordUseCase, email: String) :
    BaseViewModel<ResetPasswordState, ResetPasswordEvent, ResetPasswordEffect>(
        ResetPasswordState(email = email)
    ) {

    override fun handleEvent(event: ResetPasswordEvent) {
        when (event) {
            is ResetPasswordEvent.CodeChanged -> setState {
                copy(code = event.value, codeError = null, submitError = null)
            }
            is ResetPasswordEvent.PasswordChanged -> setState {
                copy(newPassword = event.value, passwordError = null, submitError = null)
            }
            is ResetPasswordEvent.ConfirmPasswordChanged -> setState {
                copy(
                    confirmPassword = event.value,
                    confirmPasswordError = null,
                    submitError = null
                )
            }
            ResetPasswordEvent.Submit -> submit()
            ResetPasswordEvent.BackToLogin -> sendEffect(ResetPasswordEffect.NavigateToLogin)
        }
    }

    private fun submit() {
        val s = currentState
        val codeError = AuthValidator.validateResetCode(s.code)?.message
        val passwordError = AuthValidator.validatePassword(s.newPassword)?.message
        val confirmError = passwordError?.let { null }
            ?: AuthValidator.validatePasswordsMatch(s.newPassword, s.confirmPassword)?.message

        if (codeError != null || passwordError != null || confirmError != null) {
            setState {
                copy(
                    codeError = codeError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        setState { copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            runCatching { resetPasswordUseCase(s.email, s.code, s.newPassword) }
                .onSuccess {
                    setState { copy(isSubmitting = false) }
                    sendEffect(ResetPasswordEffect.NavigateToHome)
                }
                .onFailure { t ->
                    setState { copy(isSubmitting = false, submitError = t.toUserMessage()) }
                }
        }
    }
}
