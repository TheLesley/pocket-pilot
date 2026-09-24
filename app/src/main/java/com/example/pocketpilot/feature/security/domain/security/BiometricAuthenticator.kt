package com.example.pocketpilot.feature.security.domain.security

import androidx.fragment.app.FragmentActivity
import com.example.pocketpilot.feature.security.domain.model.BiometricAuthResult
import com.example.pocketpilot.feature.security.domain.model.BiometricAvailability

/**
 * Framework-agnostic abstraction over AndroidX BiometricPrompt. Keeping the
 * activity dependency at the boundary (via [authenticate]) means the domain
 * layer can drive biometric prompts without importing AndroidX types, and
 * tests can substitute a scripted implementation.
 */
interface BiometricAuthenticator {

    fun availability(): BiometricAvailability

    /**
     * Show a biometric prompt hosted by [activity] and suspend until the user
     * or the OS produces a terminal result.
     */
    suspend fun authenticate(activity: FragmentActivity, title: String, subtitle: String, negativeButtonText: String): BiometricAuthResult
}
