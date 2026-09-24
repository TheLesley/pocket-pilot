package com.example.pocketpilot.feature.security.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocketpilot.feature.security.presentation.LockViewModel

class SecurityViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(LockViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return LockViewModel(
            observeSecurityPreferences = SecurityContainer.observeSecurityPreferences,
            checkBiometricAvailability = SecurityContainer.checkBiometricAvailability,
            verifyPinCode = SecurityContainer.verifyPinCode,
            appLockManager = SecurityContainer.appLockManager
        ) as T
    }
}
