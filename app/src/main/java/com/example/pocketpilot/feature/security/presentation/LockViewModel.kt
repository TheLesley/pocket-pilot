package com.example.pocketpilot.feature.security.presentation

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.security.domain.model.BiometricAuthResult
import com.example.pocketpilot.feature.security.domain.model.BiometricAvailability
import com.example.pocketpilot.feature.security.domain.model.PinValidation
import com.example.pocketpilot.feature.security.domain.usecase.CheckBiometricAvailabilityUseCase
import com.example.pocketpilot.feature.security.domain.usecase.ObserveSecurityPreferencesUseCase
import com.example.pocketpilot.feature.security.domain.usecase.VerifyPinCodeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel that drives the [LockScreen]. Owns the PIN scratchpad and reacts
 * to biometric prompt results; the actual prompt is driven by the composable
 * because AndroidX BiometricPrompt is anchored to a FragmentActivity and we
 * do not want that dependency in the ViewModel.
 */
class LockViewModel(
    private val observeSecurityPreferences: ObserveSecurityPreferencesUseCase,
    private val checkBiometricAvailability: CheckBiometricAvailabilityUseCase,
    private val verifyPinCode: VerifyPinCodeUseCase,
    private val appLockManager: AppLockManager
) : BaseViewModel<LockState, LockEvent, LockEffect>(LockState()) {

    private var observeJob: Job? = null

    init {
        observeSecurityState()
    }

    override fun handleEvent(event: LockEvent) {
        when (event) {
            LockEvent.PromptBiometric -> requestBiometricPrompt()
            is LockEvent.PinDigitAppended -> appendPinDigit(event.digit)
            LockEvent.PinBackspace -> backspacePin()
            LockEvent.PinCleared -> setState { copy(pinEntry = "", pinAttemptFailed = false) }
            LockEvent.SubmitPin -> submitPin()
            LockEvent.BiometricPromptDismissed ->
                setState { copy(isAuthenticating = false) }
        }
    }

    fun onBiometricResult(result: BiometricAuthResult) {
        when (result) {
            BiometricAuthResult.Success -> {
                setState { copy(isAuthenticating = false, biometricError = null) }
                appLockManager.markUnlocked()
                sendEffect(LockEffect.Unlocked)
            }
            BiometricAuthResult.UserCanceled -> {
                setState { copy(isAuthenticating = false) }
            }
            BiometricAuthResult.Failed -> {
                setState { copy(isAuthenticating = false, biometricError = "Not recognised") }
            }
            is BiometricAuthResult.Error -> {
                setState { copy(isAuthenticating = false, biometricError = result.message) }
                sendEffect(LockEffect.ShowMessage(result.message))
            }
        }
    }

    private fun observeSecurityState() {
        observeJob?.cancel()
        observeJob = observeSecurityPreferences()
            .onEach { prefs ->
                val availability = checkBiometricAvailability()
                setState {
                    copy(
                        biometricEnabled = prefs.biometricEnabled &&
                            availability == BiometricAvailability.Available,
                        hasPin = prefs.hasPin,
                        biometricAvailability = availability
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun requestBiometricPrompt() {
        if (!currentState.biometricEnabled) return
        setState { copy(isAuthenticating = true, biometricError = null) }
        sendEffect(LockEffect.LaunchBiometricPrompt)
    }

    private fun appendPinDigit(digit: Char) {
        if (!digit.isDigit()) return
        val next = currentState.pinEntry + digit
        if (next.length > PinValidation.MaxLength) return
        setState { copy(pinEntry = next, pinAttemptFailed = false) }
        if (next.length >= PinValidation.MinLength) {
            // Auto-submit at MinLength unless the user keeps typing; if
            // subsequent digits arrive we simply re-attempt on each keystroke
            // so a longer PIN unlocks without a dedicated confirm button.
            submitPin()
        }
    }

    private fun backspacePin() {
        val current = currentState.pinEntry
        if (current.isEmpty()) return
        setState { copy(pinEntry = current.dropLast(1), pinAttemptFailed = false) }
    }

    private fun submitPin() {
        val candidate = currentState.pinEntry
        if (candidate.length < PinValidation.MinLength) return
        viewModelScope.launch {
            val ok = runCatching { verifyPinCode(candidate) }.getOrDefault(false)
            if (ok) {
                setState { copy(pinEntry = "", pinAttemptFailed = false) }
                appLockManager.markUnlocked()
                sendEffect(LockEffect.Unlocked)
            } else {
                setState { copy(pinEntry = "", pinAttemptFailed = true) }
            }
        }
    }
}
