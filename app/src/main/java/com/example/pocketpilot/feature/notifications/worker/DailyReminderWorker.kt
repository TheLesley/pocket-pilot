package com.example.pocketpilot.feature.notifications.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationDispatcher
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationPreferencesRepository
import com.example.pocketpilot.feature.notifications.domain.usecase.BuildDailyReminderUseCase
import kotlinx.coroutines.CancellationException

/**
 * Fires the once-per-day "log your transactions" reminder. WorkManager
 * handles the actual scheduling window; this worker just decides whether
 * a reminder should still be posted right now based on the latest
 * preferences (the user may have flipped the toggle off between the schedule
 * being queued and the worker running).
 */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val preferencesRepository: NotificationPreferencesRepository,
    private val buildDailyReminder: BuildDailyReminderUseCase,
    private val dispatcher: NotificationDispatcher
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val prefs = preferencesRepository.get()
        val notification = buildDailyReminder(prefs)
        if (notification != null) {
            dispatcher.post(notification)
        }
        Result.success()
    } catch (ce: CancellationException) {
        throw ce
    } catch (_: Throwable) {
        if (runAttemptCount == 0) Result.retry() else Result.success()
    }
}
