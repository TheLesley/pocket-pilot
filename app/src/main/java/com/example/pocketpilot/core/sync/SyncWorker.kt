package com.example.pocketpilot.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException

/**
 * Shared body for both the [OneTimeSyncWorker] and [PeriodicSyncWorker].
 * Delegates every pass to a single [SyncManager] so background jobs go through
 * the exact same reconciliation path as an in-app "Sync now" tap, and
 * translates the resulting [SyncReport] into WorkManager's [Result] contract.
 *
 * Retry policy: transient failures (network blips, timeouts, 5xx) surface as
 * [SyncReport.failed] > 0 or a thrown [SyncException]. The worker returns
 * [Result.retry] for the first few attempts so WorkManager applies the
 * configured exponential backoff, then falls through to [Result.failure] so
 * the job stops flapping and the UI can surface the error.
 */
abstract class BaseSyncWorker(context: Context, params: WorkerParameters, private val syncManager: SyncManager) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        setProgress(runningProgress())
        return try {
            val report = syncManager.syncNow()
            val output = report.toOutputData()
            when {
                report.isFullySynced -> Result.success(output)
                report.failed == 0 -> Result.success(output)
                runAttemptCount + 1 < MAX_ATTEMPTS -> Result.retry()
                else -> Result.failure(output)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            if (runAttemptCount + 1 < MAX_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure(errorData(t))
            }
        }
    }

    private fun runningProgress(): Data = Data.Builder().putBoolean(KEY_RUNNING, true).build()

    private fun SyncReport.toOutputData(): Data = Data.Builder()
        .putInt(KEY_PUSHED, pushed)
        .putInt(KEY_FAILED, failed)
        .putInt(KEY_REMAINING, remainingPending)
        .apply { firstError?.let { putString(KEY_ERROR, it.message ?: it::class.java.simpleName) } }
        .build()

    private fun errorData(t: Throwable): Data = Data.Builder()
        .putString(KEY_ERROR, t.message ?: t::class.java.simpleName)
        .build()

    companion object {
        internal const val MAX_ATTEMPTS = 4

        const val KEY_RUNNING = "running"
        const val KEY_PUSHED = "pushed"
        const val KEY_FAILED = "failed"
        const val KEY_REMAINING = "remaining"
        const val KEY_ERROR = "error"
    }
}

/**
 * Ad-hoc "sync right now" job. Enqueued in response to explicit user actions
 * (e.g. tapping a "Sync" button, saving a transaction while online). Constraint
 * and backoff are configured by [SyncWorkScheduler.enqueueOneTimeSync].
 */
class OneTimeSyncWorker(context: Context, params: WorkerParameters, syncManager: SyncManager) :
    BaseSyncWorker(context, params, syncManager)

/**
 * Recurring background job registered once at application start. WorkManager's
 * minimum periodic interval is 15 minutes, which matches our appetite for
 * catching up any offline mutations without hammering the battery.
 */
class PeriodicSyncWorker(context: Context, params: WorkerParameters, syncManager: SyncManager) :
    BaseSyncWorker(context, params, syncManager)
