package com.example.pocketpilot.feature.notifications.domain.model

/**
 * Logical grouping of notifications by purpose. Each value maps 1:1 to an
 * Android [android.app.NotificationChannel] created at app start. Keeping the
 * enum in the domain layer means use cases can classify a message without
 * pulling in the Android framework — the data layer looks up the actual
 * channel id from [id].
 */
enum class NotificationChannelType(val id: String) {
    BUDGET_ALERTS(id = "pocketpilot.channel.budget_alerts"),
    TRANSACTION_ALERTS(id = "pocketpilot.channel.transaction_alerts"),
    REMINDERS(id = "pocketpilot.channel.reminders")
}
