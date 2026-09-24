package com.example.pocketpilot.core.sync

/**
 * Tracks whether a locally-persisted row has been reconciled with the remote
 * backend. Every syncable Room entity carries one of these values so the
 * [SyncManager] can iterate over the pending backlog when connectivity returns.
 *
 *  - [SYNCED]          – local copy matches the last known server state
 *  - [PENDING_INSERT]  – created offline, not yet POSTed
 *  - [PENDING_UPDATE]  – mutated offline, not yet PATCHed / PUT
 *  - [PENDING_DELETE]  – deleted offline; row is retained as a tombstone
 *                        until the remote DELETE succeeds
 */
enum class SyncStatus {
    SYNCED,
    PENDING_INSERT,
    PENDING_UPDATE,
    PENDING_DELETE
}
