package com.example.pocketpilot.feature.notifications.domain.model

import androidx.compose.runtime.Immutable

/**
 * User-facing switches for the alert engine. Kept as a single value type so
 * ViewModels observe one atomic stream rather than juggling one flag per
 * toggle — the settings screen renders every switch off the same snapshot.
 *
 * [budgetWarningThresholdPercent] is stored as a whole-number percent (0..100)
 * so DataStore serialisation stays boring; the alert engine divides by 100f
 * when comparing against [com.example.pocketpilot.feature.finance.domain.model.BudgetProgress.ratio].
 * [dailyReminderHourOfDay] / [dailyReminderMinute] use the device's local
 * timezone so a reminder "at 8pm" fires at 8pm wherever the user happens to be.
 */
@Immutable
data class NotificationPreferences(
    val masterEnabled: Boolean = true,
    val budgetAlertsEnabled: Boolean = true,
    val budgetWarningThresholdPercent: Int = DEFAULT_WARNING_PERCENT,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHourOfDay: Int = DEFAULT_REMINDER_HOUR,
    val dailyReminderMinute: Int = DEFAULT_REMINDER_MINUTE
) {
    val warningRatio: Float get() = budgetWarningThresholdPercent.coerceIn(0, 100) / 100f

    companion object {
        const val DEFAULT_WARNING_PERCENT: Int = 80
        const val DEFAULT_REMINDER_HOUR: Int = 20
        const val DEFAULT_REMINDER_MINUTE: Int = 0
        val Default: NotificationPreferences = NotificationPreferences()
    }
}
