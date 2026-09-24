package com.example.pocketpilot.feature.notifications.worker

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Wraps [WorkManager] enqueues for the alert engine so the choice of
 * constraints, cadence, and unique-work names lives in exactly one place.
 *
 * - Budget alerts run every [BUDGET_ALERT_INTERVAL_MINUTES] minutes (WorkManager's
 *   15-minute floor is the tightest cadence the platform allows).
 * - The daily reminder is queued as a chained one-time job that reschedules
 *   itself. Periodic work would drift because WorkManager only guarantees the
 *   *interval*, not the exact time-of-day.
 *
 * `KEEP` policies mean the schedulers are safe to call on every app start;
 * WorkManager will not disturb a queue that's already primed.
 */
class NotificationWorkScheduler(private val workManager: WorkManager) {

    /** Register the recurring budget-alert evaluation. Safe to call repeatedly. */
    fun enqueueBudgetAlerts() {
        val request = PeriodicWorkRequestBuilder<BudgetAlertWorker>(
            BUDGET_ALERT_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(TAG_NOTIFICATIONS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            NAME_BUDGET_ALERTS,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelBudgetAlerts() {
        workManager.cancelUniqueWork(NAME_BUDGET_ALERTS)
    }

    /**
     * Enqueue the next daily-reminder run at [preferences]' configured
     * time-of-day. Uses [ExistingWorkPolicy.REPLACE] because the user might
     * have just changed the time in settings — the old queue entry needs to
     * be discarded so it doesn't fire at the stale hour first.
     */
    fun rescheduleDailyReminder(preferences: NotificationPreferences) {
        cancelDailyReminder()
        if (!preferences.masterEnabled || !preferences.dailyReminderEnabled) return
        val delayMillis = nextDelayMillisFor(preferences)
        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(TAG_NOTIFICATIONS)
            .build()
        workManager.enqueueUniqueWork(
            NAME_DAILY_REMINDER,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelDailyReminder() {
        workManager.cancelUniqueWork(NAME_DAILY_REMINDER)
    }

    fun cancelAll() {
        workManager.cancelAllWorkByTag(TAG_NOTIFICATIONS)
    }

    /**
     * Computes the delay in millis until the next occurrence of
     * `hh:mm` in the device's local timezone. If today's slot is in the past
     * we schedule for tomorrow instead so a "just enabled reminders" tap does
     * not fire an alert immediately.
     */
    private fun nextDelayMillisFor(preferences: NotificationPreferences): Long {
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, preferences.dailyReminderHourOfDay)
            set(Calendar.MINUTE, preferences.dailyReminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    companion object {
        const val TAG_NOTIFICATIONS = "pocketpilot.notifications"
        const val NAME_BUDGET_ALERTS = "pocketpilot.notifications.budgetAlerts"
        const val NAME_DAILY_REMINDER = "pocketpilot.notifications.dailyReminder"

        /** WorkManager's minimum periodic interval. */
        const val BUDGET_ALERT_INTERVAL_MINUTES: Long = 15L
    }
}
