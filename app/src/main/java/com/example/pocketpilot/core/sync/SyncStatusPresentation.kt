package com.example.pocketpilot.core.sync

/**
 * Renders a [SyncWorkStatus] into a compact human-readable label. Kept as a
 * plain function (rather than living inside a composable) so unit tests can
 * assert the copy without spinning up Compose.
 */
fun SyncWorkStatus.toDisplayLabel(): String = when (this) {
    SyncWorkStatus.Idle -> "Up to date"
    SyncWorkStatus.Enqueued -> "Sync queued…"
    is SyncWorkStatus.Running -> "Syncing…"
    is SyncWorkStatus.Succeeded -> if (remainingPending == 0) {
        "Synced"
    } else {
        "Synced — $remainingPending pending"
    }
    is SyncWorkStatus.Failed -> message?.let { "Sync failed: $it" } ?: "Sync failed"
    SyncWorkStatus.Cancelled -> "Sync cancelled"
}

/**
 * True whenever the sync surface should hint at ongoing work — used by the
 * status bar to swap in a progress indicator.
 */
val SyncWorkStatus.isActive: Boolean
    get() = this is SyncWorkStatus.Enqueued || this is SyncWorkStatus.Running
