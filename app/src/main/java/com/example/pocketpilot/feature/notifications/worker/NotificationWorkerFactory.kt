package com.example.pocketpilot.feature.notifications.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.pocketpilot.feature.finance.domain.model.BudgetProgress
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationDispatcher
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationPreferencesRepository
import com.example.pocketpilot.feature.notifications.domain.usecase.BuildDailyReminderUseCase
import com.example.pocketpilot.feature.notifications.domain.usecase.EvaluateBudgetAlertsUseCase
import kotlinx.coroutines.flow.Flow

/**
 * Constructor-injects the notification-side dependencies into the workers.
 * Registered alongside `SyncWorkerFactory` in the app's WorkManager
 * `Configuration.Provider` via a `DelegatingWorkerFactory` so each factory
 * still owns exactly one worker family.
 *
 * Returning `null` for unknown worker names is intentional — it lets the
 * delegating factory continue asking the next candidate.
 */
class NotificationWorkerFactory(
    private val observeBudgetsProgress: () -> Flow<List<BudgetProgress>>,
    private val preferencesRepository: NotificationPreferencesRepository,
    private val evaluateBudgetAlerts: EvaluateBudgetAlertsUseCase,
    private val buildDailyReminder: BuildDailyReminderUseCase,
    private val dispatcher: NotificationDispatcher
) : WorkerFactory() {

    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
        when (workerClassName) {
            BudgetAlertWorker::class.java.name -> BudgetAlertWorker(
                context = appContext,
                params = workerParameters,
                observeBudgetsProgress = observeBudgetsProgress,
                preferencesRepository = preferencesRepository,
                evaluateBudgetAlerts = evaluateBudgetAlerts,
                dispatcher = dispatcher
            )
            DailyReminderWorker::class.java.name -> DailyReminderWorker(
                context = appContext,
                params = workerParameters,
                preferencesRepository = preferencesRepository,
                buildDailyReminder = buildDailyReminder,
                dispatcher = dispatcher
            )
            else -> null
        }
}
