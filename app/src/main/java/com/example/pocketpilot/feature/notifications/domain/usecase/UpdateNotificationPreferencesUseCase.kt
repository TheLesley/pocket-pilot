package com.example.pocketpilot.feature.notifications.domain.usecase

import com.example.pocketpilot.feature.notifications.domain.repository.NotificationPreferencesRepository

/**
 * Small facade over [NotificationPreferencesRepository] mutators so the
 * ViewModel depends on a single use case rather than one per switch.
 */
class UpdateNotificationPreferencesUseCase(private val repository: NotificationPreferencesRepository) {
    suspend fun setMasterEnabled(enabled: Boolean) = repository.setMasterEnabled(enabled)

    suspend fun setBudgetAlertsEnabled(enabled: Boolean) = repository.setBudgetAlertsEnabled(enabled)

    suspend fun setBudgetWarningThresholdPercent(percent: Int) = repository.setBudgetWarningThresholdPercent(percent.coerceIn(0, 100))

    suspend fun setDailyReminderEnabled(enabled: Boolean) = repository.setDailyReminderEnabled(enabled)

    suspend fun setDailyReminderTime(hourOfDay: Int, minute: Int) = repository.setDailyReminderTime(
        hourOfDay = hourOfDay.coerceIn(0, 23),
        minute = minute.coerceIn(0, 59)
    )
}
