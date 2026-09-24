package com.example.pocketpilot.feature.notifications.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationDispatcher
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationPreferencesRepository
import com.example.pocketpilot.feature.notifications.domain.usecase.EvaluateBudgetAlertsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/**
 * Periodic pass that evaluates every active budget and posts a warning or
 * "exceeded" alert if the user's threshold is crossed. Notification ids are
 * derived from the budget id in
 * [EvaluateBudgetAlertsUseCase], so re-running does not stack duplicates.
 *
 * A single failure returns [Result.retry] once; on the second attempt we drop
 * to [Result.success] because a stuck alert pass should not consume battery.
 */
class BudgetAlertWorker(
    context: Context,
    params: WorkerParameters,
    private val observeBudgetsProgress:
    () -> kotlinx.coroutines.flow.Flow<List<com.example.pocketpilot.feature.finance.domain.model.BudgetProgress>>,
    private val preferencesRepository: NotificationPreferencesRepository,
    private val evaluateBudgetAlerts: EvaluateBudgetAlertsUseCase,
    private val dispatcher: NotificationDispatcher
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val prefs = preferencesRepository.get()
        if (!prefs.masterEnabled || !prefs.budgetAlertsEnabled) {
            Result.success()
        } else {
            val progresses = observeBudgetsProgress().first()
            val alerts = evaluateBudgetAlerts(progresses, prefs)
            alerts.forEach { dispatcher.post(it) }
            Result.success()
        }
    } catch (ce: CancellationException) {
        throw ce
    } catch (_: Throwable) {
        if (runAttemptCount == 0) Result.retry() else Result.success()
    }
}
