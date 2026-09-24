package com.example.pocketpilot.core.sync

import androidx.compose.runtime.Immutable

/**
 * Coarse-grained state surfaced to the UI so users can see whether the app
 * currently has pending offline changes waiting to be uploaded.
 */
@Immutable
sealed interface SyncState {
    data object Idle : SyncState
    data object InProgress : SyncState
    data object Offline : SyncState

    @Immutable data class Success(val syncedAtEpochMillis: Long, val pushed: Int) : SyncState

    @Immutable data class Failed(val throwable: Throwable, val remainingPending: Int) : SyncState
}
