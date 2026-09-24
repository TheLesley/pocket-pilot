package com.example.pocketpilot.feature.notifications.domain.repository

import com.example.pocketpilot.feature.notifications.domain.model.AlertNotification

/**
 * Abstraction that hides Android's `NotificationManager` from the domain layer
 * so use cases stay unit-testable. The data-layer implementation is a thin
 * `NotificationCompat` wrapper that also honours the runtime POST_NOTIFICATIONS
 * permission and the master notifications toggle.
 */
interface NotificationDispatcher {

    /** Returns `false` if the platform blocked the post (permission denied,
     * channel muted, master toggle off). Callers can log or ignore. */
    fun post(notification: AlertNotification): Boolean

    fun cancel(id: Int)

    /** Whether notifications may be posted at all right now. UI uses this to
     * decide when to prompt for the runtime permission on Android 13+. */
    fun areNotificationsAllowed(): Boolean
}
