package com.example.pocketpilot.feature.security.presentation

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.security.domain.model.BiometricAvailability

@Immutable
data class LockState(
    val biometricAvailability: BiometricAvailability = BiometricAvailability.Unknown,
    val biometricEnabled: Boolean = false,
    val hasPin: Boolean = false,
    val pinEntry: String = "",
    val pinAttemptFailed: Boolean = false,
    val biometricError: String? = null,
    val isAuthenticating: Boolean = false
)

sealed interface LockEvent : UiEvent {
    data object PromptBiometric : LockEvent
    data class PinDigitAppended(val digit: Char) : LockEvent
    data object PinBackspace : LockEvent
    data object PinCleared : LockEvent
    data object SubmitPin : LockEvent
    data object BiometricPromptDismissed : LockEvent
}

sealed interface LockEffect : UiEffect {
    data object Unlocked : LockEffect
    data class ShowMessage(val message: String) : LockEffect

    /**
     * The ViewModel cannot own the FragmentActivity, so the composable listens
     * for this effect and drives the AndroidX BiometricPrompt itself.
     */
    data object LaunchBiometricPrompt : LockEffect
}
