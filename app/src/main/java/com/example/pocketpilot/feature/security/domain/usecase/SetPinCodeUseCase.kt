package com.example.pocketpilot.feature.security.domain.usecase

import com.example.pocketpilot.feature.security.domain.model.PinValidation
import com.example.pocketpilot.feature.security.domain.repository.SecurityRepository
import com.example.pocketpilot.feature.security.domain.security.PinHasher

/**
 * Validate a candidate PIN, salt+hash it, and persist. Returns the validation
 * result so callers can render a specific error message on failure.
 */
class SetPinCodeUseCase(private val repository: SecurityRepository, private val pinHasher: PinHasher) {
    suspend operator fun invoke(pin: String): PinValidation {
        val validation = PinValidation.validate(pin)
        if (validation != PinValidation.Valid) return validation

        val salt = pinHasher.newSalt()
        val hash = pinHasher.hash(pin, salt)
        repository.setPin(hash = hash, salt = salt)
        return PinValidation.Valid
    }
}
