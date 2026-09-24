package com.example.pocketpilot.core.sync

/**
 * How the [SyncManager] should reconcile a local `PENDING_UPDATE` with a
 * fresher server record.
 *
 *  - [SERVER_WINS]       – always accept the server payload, discarding the
 *                          local mutation. Safest default; guarantees the
 *                          client eventually converges on the server view.
 *  - [CLIENT_TIMESTAMP]  – compare `updatedAt` timestamps; keep whichever side
 *                          was mutated most recently. Useful for last-write-wins
 *                          workflows where the user's most recent edit should
 *                          not be overwritten by a stale server value.
 */
enum class ConflictStrategy {
    SERVER_WINS,
    CLIENT_TIMESTAMP
}
