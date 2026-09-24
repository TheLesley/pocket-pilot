package com.example.pocketpilot.feature.notifications.domain.model

import androidx.compose.runtime.Immutable

/**
 * Framework-agnostic description of a notification the app wants to post.
 * Use cases produce these; the data-layer dispatcher translates them into a
 * `NotificationCompat.Builder` on Android. Keeping `id` in the domain layer
 * lets a subsequent evaluation replace or cancel a specific alert (e.g. clear
 * an "80% used" warning once the budget flips to "exceeded").
 */
@Immutable
data class AlertNotification(
    val id: Int,
    val channel: NotificationChannelType,
    val title: String,
    val body: String,
    val category: AlertCategory
)

/**
 * What the notification is *about*. Used by the dispatcher to pick priority
 * defaults and by tests to assert the correct alert fired without depending
 * on the exact copy string.
 */
enum class AlertCategory {
    BUDGET_WARNING,
    BUDGET_EXCEEDED,
    DAILY_REMINDER
}
