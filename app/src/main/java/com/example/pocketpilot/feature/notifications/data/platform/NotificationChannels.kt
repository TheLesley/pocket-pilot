package com.example.pocketpilot.feature.notifications.data.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.example.pocketpilot.feature.notifications.domain.model.NotificationChannelType

/**
 * Creates every [NotificationChannel] the app posts to. Idempotent — the
 * system deduplicates channels by id — so calling it on every app start is
 * fine and covers upgrades that need to re-register a channel with a new
 * name or description.
 *
 * On API < 26 there are no channels; this function is a no-op there.
 */
internal object NotificationChannels {

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                NotificationChannelType.BUDGET_ALERTS.id,
                "Budget alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warnings when a budget is close to or over its limit."
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                NotificationChannelType.TRANSACTION_ALERTS.id,
                "Transaction alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about unusual or large transactions."
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                NotificationChannelType.REMINDERS.id,
                "Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily reminders to log transactions and review your budget."
            }
        )
    }
}
