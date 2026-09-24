package com.example.pocketpilot.feature.notifications.data.platform

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.example.pocketpilot.R
import com.example.pocketpilot.feature.notifications.domain.model.AlertCategory
import com.example.pocketpilot.feature.notifications.domain.model.AlertNotification
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationDispatcher
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationPreferencesRepository
import kotlinx.coroutines.runBlocking

/**
 * Concrete [NotificationDispatcher] backed by [NotificationManagerCompat]. Two
 * gates block a post:
 *
 *  1. The runtime `POST_NOTIFICATIONS` permission (Android 13+).
 *  2. The user's own [com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences.masterEnabled]
 *     switch — checked here (in addition to inside the workers) so a stray
 *     dispatcher call from a future entry point can't sneak past the toggle.
 *
 * Reading the master switch requires a suspend call, which the WorkManager
 * `CoroutineWorker` already runs inside a coroutine. `runBlocking` is used
 * only when [post] is called from a plain-thread context (e.g. tests, direct
 * one-shot alerts); the DataStore read is cheap and short-circuited by
 * DataStore's own in-memory cache after the first hit.
 */
class AndroidNotificationDispatcher(private val context: Context, private val preferencesRepository: NotificationPreferencesRepository) :
    NotificationDispatcher {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context.applicationContext)

    init {
        NotificationChannels.ensureCreated(context.applicationContext)
    }

    override fun post(notification: AlertNotification): Boolean {
        if (!areNotificationsAllowed()) return false
        val prefs = runBlocking { preferencesRepository.get() }
        if (!prefs.masterEnabled) return false

        val builder = NotificationCompat.Builder(context, notification.channel.id)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setPriority(notification.category.compatPriority())
            .setCategory(notification.category.notificationCategory())
            .setAutoCancel(true)

        return try {
            notificationManager.notify(notification.id, builder.build())
            true
        } catch (_: SecurityException) {
            // Race with the user revoking POST_NOTIFICATIONS between the
            // check and the post — treat it as a soft failure.
            false
        }
    }

    override fun cancel(id: Int) {
        notificationManager.cancel(id)
    }

    override fun areNotificationsAllowed(): Boolean {
        if (!NotificationPermission.isGranted(context)) return false
        if (!notificationManager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // If the system-wide toggle is on but the specific channel is
            // muted, `NotificationManagerCompat.areNotificationsEnabled()`
            // still returns true — the channel-level check has to be done
            // by inspecting each channel's importance.
            val systemManager = context.getSystemService<NotificationManager>() ?: return true
            val anyChannelEnabled = systemManager.notificationChannels
                .any { it.importance != NotificationManager.IMPORTANCE_NONE }
            if (!anyChannelEnabled) return false
        }
        return true
    }
}

private fun AlertCategory.compatPriority(): Int = when (this) {
    AlertCategory.BUDGET_EXCEEDED -> NotificationCompat.PRIORITY_HIGH
    AlertCategory.BUDGET_WARNING -> NotificationCompat.PRIORITY_DEFAULT
    AlertCategory.DAILY_REMINDER -> NotificationCompat.PRIORITY_LOW
}

private fun AlertCategory.notificationCategory(): String = when (this) {
    AlertCategory.BUDGET_EXCEEDED,
    AlertCategory.BUDGET_WARNING -> NotificationCompat.CATEGORY_STATUS
    AlertCategory.DAILY_REMINDER -> NotificationCompat.CATEGORY_REMINDER
}
