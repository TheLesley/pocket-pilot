package com.example.pocketpilot.feature.security.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.pocketpilot.feature.security.domain.model.BiometricAuthResult
import com.example.pocketpilot.feature.security.domain.model.BiometricAvailability
import com.example.pocketpilot.feature.security.domain.security.BiometricAuthenticator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * AndroidX BiometricPrompt implementation of [BiometricAuthenticator]. Both
 * [BIOMETRIC_STRONG] and [BIOMETRIC_WEAK] are accepted so face-only devices
 * (which advertise as WEAK) can still unlock the app — the PIN fallback
 * remains the last line of defence for anyone without enrolled biometrics.
 */
class BiometricPromptAuthenticator(private val appContext: Context) : BiometricAuthenticator {

    private val allowedAuthenticators = BIOMETRIC_STRONG or BIOMETRIC_WEAK

    override fun availability(): BiometricAvailability {
        val manager = BiometricManager.from(appContext)
        return when (manager.canAuthenticate(allowedAuthenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                BiometricAvailability.SecurityUpdateRequired
            else -> BiometricAvailability.Unknown
        }
    }

    override suspend fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String
    ): BiometricAuthResult = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)
            val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (continuation.isActive) {
                            continuation.resume(BiometricAuthResult.Success)
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (!continuation.isActive) return
                        val outcome = when (errorCode) {
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                            BiometricPrompt.ERROR_CANCELED -> BiometricAuthResult.UserCanceled
                            else -> BiometricAuthResult.Error(
                                code = errorCode,
                                message = errString.toString()
                            )
                        }
                        continuation.resume(outcome)
                    }

                    override fun onAuthenticationFailed() {
                        // Non-terminal: BiometricPrompt keeps the dialog open,
                        // so we do NOT resume the continuation here. The UI
                        // can surface a transient toast through its own hook
                        // if we later expose one.
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(allowedAuthenticators)
                .setConfirmationRequired(false)
                .build()

            continuation.invokeOnCancellation { prompt.cancelAuthentication() }
            prompt.authenticate(promptInfo)
        }
    }
}
