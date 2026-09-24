package com.example.pocketpilot.feature.security.domain.usecase

import com.example.pocketpilot.feature.security.domain.model.BiometricAvailability
import com.example.pocketpilot.feature.security.domain.security.BiometricAuthenticator

class CheckBiometricAvailabilityUseCase(private val authenticator: BiometricAuthenticator) {
    operator fun invoke(): BiometricAvailability = authenticator.availability()
}
