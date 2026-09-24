package com.example.pocketpilot.feature.security.di

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.example.pocketpilot.feature.security.data.local.SecurityPreferencesDataSource
import com.example.pocketpilot.feature.security.data.repository.SecurityRepositoryImpl
import com.example.pocketpilot.feature.security.data.security.BiometricPromptAuthenticator
import com.example.pocketpilot.feature.security.data.security.Sha256PinHasher
import com.example.pocketpilot.feature.security.domain.model.BiometricAuthResult
import com.example.pocketpilot.feature.security.domain.repository.SecurityRepository
import com.example.pocketpilot.feature.security.domain.security.BiometricAuthenticator
import com.example.pocketpilot.feature.security.domain.security.PinHasher
import com.example.pocketpilot.feature.security.domain.usecase.AuthenticateWithBiometricUseCase
import com.example.pocketpilot.feature.security.domain.usecase.CheckBiometricAvailabilityUseCase
import com.example.pocketpilot.feature.security.domain.usecase.ClearPinCodeUseCase
import com.example.pocketpilot.feature.security.domain.usecase.ObserveSecurityPreferencesUseCase
import com.example.pocketpilot.feature.security.domain.usecase.SetPinCodeUseCase
import com.example.pocketpilot.feature.security.domain.usecase.UpdateAutoLockTimeoutUseCase
import com.example.pocketpilot.feature.security.domain.usecase.UpdateBiometricEnabledUseCase
import com.example.pocketpilot.feature.security.domain.usecase.VerifyPinCodeUseCase
import com.example.pocketpilot.feature.security.presentation.AppLockManager

/**
 * Service locator for the security feature. Mirrors the shape of the other
 * feature containers (settings, notifications) so the eventual Hilt migration
 * is mechanical.
 */
object SecurityContainer {

    @Volatile
    private var dataSource: SecurityPreferencesDataSource? = null

    @Volatile
    private var biometricAuthenticator: BiometricAuthenticator? = null

    @Volatile
    private var lockManager: AppLockManager? = null

    fun init(context: Context) {
        if (dataSource == null) {
            synchronized(this) {
                if (dataSource == null) {
                    val appContext = context.applicationContext
                    dataSource = SecurityPreferencesDataSource.create(appContext)
                    biometricAuthenticator = BiometricPromptAuthenticator(appContext)
                }
            }
        }
    }

    private fun requireDataSource(): SecurityPreferencesDataSource = checkNotNull(dataSource) {
        "SecurityContainer.init(context) must be called before accessing security state."
    }

    private fun requireBiometricAuthenticator(): BiometricAuthenticator = checkNotNull(biometricAuthenticator) {
        "SecurityContainer.init(context) must be called before accessing security state."
    }

    val securityRepository: SecurityRepository by lazy {
        SecurityRepositoryImpl(requireDataSource())
    }

    val pinHasher: PinHasher by lazy { Sha256PinHasher() }

    val observeSecurityPreferences: ObserveSecurityPreferencesUseCase by lazy {
        ObserveSecurityPreferencesUseCase(securityRepository)
    }

    val updateBiometricEnabled: UpdateBiometricEnabledUseCase by lazy {
        UpdateBiometricEnabledUseCase(securityRepository)
    }

    val updateAutoLockTimeout: UpdateAutoLockTimeoutUseCase by lazy {
        UpdateAutoLockTimeoutUseCase(securityRepository)
    }

    val setPinCode: SetPinCodeUseCase by lazy {
        SetPinCodeUseCase(securityRepository, pinHasher)
    }

    val verifyPinCode: VerifyPinCodeUseCase by lazy {
        VerifyPinCodeUseCase(securityRepository, pinHasher)
    }

    val clearPinCode: ClearPinCodeUseCase by lazy {
        ClearPinCodeUseCase(securityRepository)
    }

    val checkBiometricAvailability: CheckBiometricAvailabilityUseCase by lazy {
        CheckBiometricAvailabilityUseCase(requireBiometricAuthenticator())
    }

    private val authenticateWithBiometricUseCase: AuthenticateWithBiometricUseCase by lazy {
        AuthenticateWithBiometricUseCase(requireBiometricAuthenticator())
    }

    /**
     * Convenience passthrough so the [com.example.pocketpilot.feature.security
     * .presentation.LockRoute] can drive the biometric prompt without holding
     * the use case directly.
     */
    suspend fun authenticateWithBiometric(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String
    ): BiometricAuthResult = authenticateWithBiometricUseCase(
        activity = activity,
        title = title,
        subtitle = subtitle,
        negativeButtonText = negativeButtonText
    )

    val appLockManager: AppLockManager
        get() = lockManager ?: synchronized(this) {
            lockManager ?: AppLockManager(
                observeSecurityPreferences = observeSecurityPreferences
            ).also { lockManager = it }
        }
}
