package com.example.pocketpilot.feature.security.domain.usecase

import com.example.pocketpilot.feature.security.domain.repository.SecurityRepository
import com.example.pocketpilot.feature.security.domain.security.PinHasher

/**
 * Constant-time-ish PIN verification. Returns `false` when no PIN is
 * configured so the caller can steer the user to the setup flow instead of
 * silently succeeding.
 */
class VerifyPinCodeUseCase(private val repository: SecurityRepository, private val pinHasher: PinHasher) {
    suspend operator fun invoke(pin: String): Boolean {
        val prefs = repository.current()
        if (!prefs.hasPin) return false
        val candidate = pinHasher.hash(pin, prefs.pinSalt)
        return constantTimeEquals(candidate, prefs.pinHash)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var mismatch = 0
        for (i in a.indices) {
            mismatch = mismatch or (a[i].code xor b[i].code)
        }
        return mismatch == 0
    }
}
