package com.example.pocketpilot.core.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

/**
 * Thin wrapper over [WorkManager] that keeps every sync-related enqueue in one
 * file. Callers (Application startup, ViewModels reacting to a "Sync now" tap)
 * never touch WorkManager directly — that isolates the choice of constraints,
 * backoff, and unique-work names so tweaking them can't drift across the app.
 *
 * The [status] flow projects the latest [WorkInfo] for either the periodic or
 * one-time job into a UI-friendly [SyncWorkStatus], preferring whichever job
 * is most "active" so the surface reflects an in-flight sync even while the
 * periodic entry sits in [WorkInfo.State.ENQUEUED] between runs.
 */
class SyncWorkScheduler(private val workManager: WorkManager) {

    /**
     * Registers the recurring background sync. Uses [ExistingPeriodicWorkPolicy.KEEP]
     * so app restarts do not disturb an already-scheduled cycle — WorkManager
     * would otherwise reset the interval and re-arm the backoff clock.
     */
    fun enqueuePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(
            PERIODIC_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(defaultConstraints())
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(TAG_SYNC)
            .build()
        workManager.enqueueUniquePeriodicWork(
            NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Requests an immediate sync pass. [ExistingWorkPolicy.KEEP] guards against
     * a user hammering "Sync now" while a pass is already queued — the second
     * request simply piggy-backs on the pending one.
     */
    fun enqueueOneTimeSync() {
        val request = OneTimeWorkRequestBuilder<OneTimeSyncWorker>()
            .setConstraints(defaultConstraints())
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(TAG_SYNC)
            .build()
        workManager.enqueueUniqueWork(
            NAME_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    /** Cancels both queued and running sync jobs. */
    fun cancelAll() {
        workManager.cancelAllWorkByTag(TAG_SYNC)
    }

    val status: Flow<SyncWorkStatus> =
        workManager.getWorkInfosByTagFlow(TAG_SYNC)
            .map { infos -> infos.pickMostRelevant().toSyncWorkStatus() }
            .distinctUntilChanged()

    private fun defaultConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * Picks the [WorkInfo] the UI most likely cares about. A running or
     * enqueued one-time request beats a periodic one waiting for its next
     * window, so users see progress from taps immediately. Falls back to the
     * newest terminal entry so `Success` / `Failed` don't vanish the moment
     * the job settles.
     */
    private fun List<WorkInfo>.pickMostRelevant(): WorkInfo? {
        if (isEmpty()) return null
        val priority: (WorkInfo.State) -> Int = { state ->
            when (state) {
                WorkInfo.State.RUNNING -> 0
                WorkInfo.State.ENQUEUED -> 1
                WorkInfo.State.BLOCKED -> 2
                WorkInfo.State.FAILED -> 3
                WorkInfo.State.SUCCEEDED -> 4
                WorkInfo.State.CANCELLED -> 5
            }
        }
        return sortedBy { priority(it.state) }.first()
    }

    private fun WorkInfo?.toSyncWorkStatus(): SyncWorkStatus {
        if (this == null) return SyncWorkStatus.Idle
        return when (state) {
            WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> SyncWorkStatus.Enqueued
            WorkInfo.State.RUNNING -> SyncWorkStatus.Running(
                pushed = progress.getInt(BaseSyncWorker.KEY_PUSHED, 0),
                remainingPending = progress.getInt(BaseSyncWorker.KEY_REMAINING, 0)
            )
            WorkInfo.State.SUCCEEDED -> SyncWorkStatus.Succeeded(
                pushed = outputData.getInt(BaseSyncWorker.KEY_PUSHED, 0),
                remainingPending = outputData.getInt(BaseSyncWorker.KEY_REMAINING, 0)
            )
            WorkInfo.State.FAILED -> SyncWorkStatus.Failed(
                remainingPending = outputData.getInt(BaseSyncWorker.KEY_REMAINING, 0),
                message = outputData.getString(BaseSyncWorker.KEY_ERROR)
            )
            WorkInfo.State.CANCELLED -> SyncWorkStatus.Cancelled
        }
    }

    companion object {
        const val TAG_SYNC = "pocketpilot.sync"
        const val NAME_PERIODIC = "pocketpilot.sync.periodic"
        const val NAME_ONE_TIME = "pocketpilot.sync.oneTime"

        /**
         * WorkManager rejects periodic intervals below 15 minutes, so this is
         * the tightest cadence the platform will let us run.
         */
        const val PERIODIC_INTERVAL_MINUTES = 15L
    }
}
