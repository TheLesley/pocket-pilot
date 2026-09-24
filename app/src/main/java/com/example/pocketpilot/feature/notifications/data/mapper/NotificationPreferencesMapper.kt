package com.example.pocketpilot.feature.notifications.data.mapper

import androidx.datastore.preferences.core.Preferences
import com.example.pocketpilot.feature.notifications.data.local.NotificationPreferencesDataSource
import com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences

/**
 * Explicit mapper between the DataStore [Preferences] view and the
 * [NotificationPreferences] domain model. Missing keys fall back to the
 * domain defaults so a fresh install has sensible alerting behaviour out of
 * the box (master + budget alerts on, warning at 80%, daily reminder off).
 */
internal fun Preferences.toDomain(): NotificationPreferences = NotificationPreferences(
    masterEnabled = this[NotificationPreferencesDataSource.Keys.MasterEnabled]
        ?: NotificationPreferences.Default.masterEnabled,
    budgetAlertsEnabled = this[NotificationPreferencesDataSource.Keys.BudgetAlertsEnabled]
        ?: NotificationPreferences.Default.budgetAlertsEnabled,
    budgetWarningThresholdPercent = this[NotificationPreferencesDataSource.Keys.BudgetWarningThresholdPercent]
        ?: NotificationPreferences.DEFAULT_WARNING_PERCENT,
    dailyReminderEnabled = this[NotificationPreferencesDataSource.Keys.DailyReminderEnabled]
        ?: NotificationPreferences.Default.dailyReminderEnabled,
    dailyReminderHourOfDay = this[NotificationPreferencesDataSource.Keys.DailyReminderHour]
        ?: NotificationPreferences.DEFAULT_REMINDER_HOUR,
    dailyReminderMinute = this[NotificationPreferencesDataSource.Keys.DailyReminderMinute]
        ?: NotificationPreferences.DEFAULT_REMINDER_MINUTE
)
