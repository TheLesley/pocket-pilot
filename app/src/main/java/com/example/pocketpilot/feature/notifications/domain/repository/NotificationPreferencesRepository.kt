package com.example.pocketpilot.feature.notifications.domain.repository

import com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Persistence contract for [NotificationPreferences]. The observation stream
 * is the single source of truth so ViewModel state and background workers
 * both see the same value without a manual refresh.
 */
interface NotificationPreferencesRepository {

    fun observe(): Flow<NotificationPreferences>

    suspend fun get(): NotificationPreferences

    suspend fun setMasterEnabled(enabled: Boolean)

    suspend fun setBudgetAlertsEnabled(enabled: Boolean)

    suspend fun setBudgetWarningThresholdPercent(percent: Int)

    suspend fun setDailyReminderEnabled(enabled: Boolean)

    suspend fun setDailyReminderTime(hourOfDay: Int, minute: Int)
}
