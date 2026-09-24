package com.example.pocketpilot.feature.security.data.repository

import com.example.pocketpilot.feature.security.data.local.SecurityPreferencesDataSource
import com.example.pocketpilot.feature.security.domain.model.AutoLockTimeout
import com.example.pocketpilot.feature.security.domain.model.SecurityPreferences
import com.example.pocketpilot.feature.security.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class SecurityRepositoryImpl(private val dataSource: SecurityPreferencesDataSource) : SecurityRepository {

    override fun observe(): Flow<SecurityPreferences> = dataSource.preferencesFlow.distinctUntilChanged()

    override suspend fun current(): SecurityPreferences = dataSource.current()

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataSource.setBiometricEnabled(enabled)
    }

    override suspend fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        dataSource.setAutoLockTimeout(timeout)
    }

    override suspend fun setPin(hash: String, salt: String) {
        dataSource.setPin(hash = hash, salt = salt)
    }

    override suspend fun clearPin() {
        dataSource.clearPin()
    }
}
