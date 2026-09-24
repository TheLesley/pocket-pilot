package com.example.pocketpilot.core.sync

import androidx.compose.runtime.Immutable
import androidx.work.WorkInfo

/**
 * UI-facing view of the background sync worker. Mirrors [WorkInfo.State] but
 * stays independent of the WorkManager types so composables and view-models do
 * not need to depend on `androidx.work`. Carries the last-known progress
 * counters ([pushed], [remainingPending]) so the UI can render a meaningful
 * "Synced 3 items · 0 pending" summary.
 */
@Immutable
sealed interface SyncWorkStatus {
    data object Idle : SyncWorkStatus
    data object Enqueued : SyncWorkStatus

    @Immutable data class Running(val pushed: Int = 0, val remainingPending: Int = 0) : SyncWorkStatus

    @Immutable data class Succeeded(val pushed: Int, val remainingPending: Int) : SyncWorkStatus

    @Immutable data class Failed(val remainingPending: Int, val message: String?) : SyncWorkStatus
    data object Cancelled : SyncWorkStatus
}
