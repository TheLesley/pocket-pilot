package com.example.pocketpilot.feature.notifications.domain.usecase

import com.example.pocketpilot.feature.notifications.domain.model.AlertCategory
import com.example.pocketpilot.feature.notifications.domain.model.AlertNotification
import com.example.pocketpilot.feature.notifications.domain.model.NotificationChannelType
import com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences

/**
 * Constructs the daily "log your transactions" reminder. Kept in the domain
 * layer so copy tweaks don't require touching the worker.
 */
class BuildDailyReminderUseCase {

    operator fun invoke(preferences: NotificationPreferences): AlertNotification? {
        if (!preferences.masterEnabled || !preferences.dailyReminderEnabled) return null
        return AlertNotification(
            id = NOTIFICATION_ID_DAILY_REMINDER,
            channel = NotificationChannelType.REMINDERS,
            title = "Daily check-in",
            body = "Take a moment to log today's transactions and stay on top of your budget.",
            category = AlertCategory.DAILY_REMINDER
        )
    }

    companion object {
        const val NOTIFICATION_ID_DAILY_REMINDER: Int = 1_001
    }
}
