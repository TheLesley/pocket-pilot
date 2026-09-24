package com.example.pocketpilot.feature.auth.presentation.login

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.auth.domain.usecase.LoginUseCase
import com.example.pocketpilot.feature.auth.domain.validation.AuthValidator
import com.example.pocketpilot.feature.auth.presentation.util.toUserMessage
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUseCase: LoginUseCase) : BaseViewModel<LoginState, LoginEvent, LoginEffect>(LoginState()) {

    override fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> setState {
                copy(email = event.value, emailError = null, submitError = null)
            }
            is LoginEvent.PasswordChanged -> setState {
                copy(password = event.value, passwordError = null, submitError = null)
            }
            LoginEvent.Submit -> submit()
            LoginEvent.ForgotPasswordClicked -> sendEffect(LoginEffect.NavigateToForgotPassword)
            LoginEvent.SignUpClicked -> sendEffect(LoginEffect.NavigateToSignUp)
        }
    }

    private fun submit() {
        val s = currentState
        val emailError = AuthValidator.validateEmail(s.email)?.message
        val passwordError = AuthValidator.validateLoginPassword(s.password)?.message
        if (emailError != null || passwordError != null) {
            setState { copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        setState { copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            runCatching { loginUseCase(s.email, s.password) }
                .onSuccess {
                    setState { copy(isSubmitting = false) }
                    sendEffect(LoginEffect.NavigateToHome)
                }
                .onFailure { t ->
                    setState { copy(isSubmitting = false, submitError = t.toUserMessage()) }
                }
        }
    }
}
