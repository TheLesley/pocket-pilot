package com.example.pocketpilot.feature.auth.presentation.signup

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.auth.domain.usecase.SignUpUseCase
import com.example.pocketpilot.feature.auth.domain.validation.AuthValidator
import com.example.pocketpilot.feature.auth.presentation.util.toUserMessage
import kotlinx.coroutines.launch

class SignUpViewModel(private val signUpUseCase: SignUpUseCase) : BaseViewModel<SignUpState, SignUpEvent, SignUpEffect>(SignUpState()) {

    override fun handleEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.NameChanged -> setState {
                copy(name = event.value, nameError = null, submitError = null)
            }
            is SignUpEvent.EmailChanged -> setState {
                copy(email = event.value, emailError = null, submitError = null)
            }
            is SignUpEvent.PasswordChanged -> setState {
                copy(password = event.value, passwordError = null, submitError = null)
            }
            is SignUpEvent.ConfirmPasswordChanged -> setState {
                copy(
                    confirmPassword = event.value,
                    confirmPasswordError = null,
                    submitError = null
                )
            }
            SignUpEvent.Submit -> submit()
            SignUpEvent.LoginClicked -> sendEffect(SignUpEffect.NavigateToLogin)
        }
    }

    private fun submit() {
        val s = currentState
        val nameError = AuthValidator.validateName(s.name)?.message
        val emailError = AuthValidator.validateEmail(s.email)?.message
        val passwordError = AuthValidator.validatePassword(s.password)?.message
        val confirmError = passwordError?.let { null }
            ?: AuthValidator.validatePasswordsMatch(s.password, s.confirmPassword)?.message

        if (nameError != null || emailError != null || passwordError != null || confirmError != null) {
            setState {
                copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        setState { copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            runCatching { signUpUseCase(s.name, s.email, s.password) }
                .onSuccess {
                    setState { copy(isSubmitting = false) }
                    sendEffect(SignUpEffect.NavigateToHome)
                }
                .onFailure { t ->
                    setState { copy(isSubmitting = false, submitError = t.toUserMessage()) }
                }
        }
    }
}
