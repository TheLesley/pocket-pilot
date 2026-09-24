package com.example.pocketpilot.feature.notifications.data.repository

import com.example.pocketpilot.feature.notifications.data.local.NotificationPreferencesDataSource
import com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class NotificationPreferencesRepositoryImpl(private val dataSource: NotificationPreferencesDataSource) :
    NotificationPreferencesRepository {

    override fun observe(): Flow<NotificationPreferences> = dataSource.preferencesFlow.distinctUntilChanged()

    override suspend fun get(): NotificationPreferences = dataSource.get()

    override suspend fun setMasterEnabled(enabled: Boolean) {
        dataSource.setMasterEnabled(enabled)
    }

    override suspend fun setBudgetAlertsEnabled(enabled: Boolean) {
        dataSource.setBudgetAlertsEnabled(enabled)
    }

    override suspend fun setBudgetWarningThresholdPercent(percent: Int) {
        dataSource.setBudgetWarningThresholdPercent(percent)
    }

    override suspend fun setDailyReminderEnabled(enabled: Boolean) {
        dataSource.setDailyReminderEnabled(enabled)
    }

    override suspend fun setDailyReminderTime(hourOfDay: Int, minute: Int) {
        dataSource.setDailyReminderTime(hourOfDay = hourOfDay, minute = minute)
    }
}
