package com.example.pocketpilot.core.sync

/**
 * Aggregate outcome of a single sync pass. `pushed` counts successful
 * insert/update/delete operations; `failed` counts operations that raised.
 * `remainingPending` reflects how many rows across all syncable tables still
 * carry a non-`SYNCED` status when the pass finishes.
 */
data class SyncReport(val pushed: Int, val failed: Int, val remainingPending: Int, val errors: List<Throwable>) {
    val isFullySynced: Boolean get() = failed == 0 && remainingPending == 0
    val firstError: Throwable? get() = errors.firstOrNull()

    companion object {
        val Empty = SyncReport(pushed = 0, failed = 0, remainingPending = 0, errors = emptyList())
    }
}
