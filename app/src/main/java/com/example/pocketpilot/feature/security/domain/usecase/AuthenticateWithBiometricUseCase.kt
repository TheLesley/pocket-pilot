package com.example.pocketpilot.feature.security.domain.usecase

import androidx.fragment.app.FragmentActivity
import com.example.pocketpilot.feature.security.domain.model.BiometricAuthResult
import com.example.pocketpilot.feature.security.domain.security.BiometricAuthenticator

/**
 * Thin use case that delegates to a [BiometricAuthenticator]. The activity
 * argument is required because AndroidX BiometricPrompt is anchored to a
 * FragmentActivity — keeping it at the use-case boundary means the ViewModel
 * can stay activity-agnostic apart from a single callback.
 */
class AuthenticateWithBiometricUseCase(private val authenticator: BiometricAuthenticator) {
    suspend operator fun invoke(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String
    ): BiometricAuthResult = authenticator.authenticate(
        activity = activity,
        title = title,
        subtitle = subtitle,
        negativeButtonText = negativeButtonText
    )
}
