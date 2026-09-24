package com.example.pocketpilot.core.sync

import com.example.pocketpilot.core.network.NetworkException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Reconciles locally-cached data with the backend. A [SyncManager] holds a
 * collection of [EntitySync] strategies (one per syncable table) and drives
 * them in a single pass — either explicitly via [syncNow] or automatically the
 * next time the [NetworkMonitor] reports connectivity.
 *
 * The manager is intentionally decoupled from any specific entity type: adding
 * a new syncable table is a matter of registering another [EntitySync] with
 * the constructor. A single-slot [Mutex] serialises passes so a
 * connectivity-triggered pass cannot race with a manual [syncNow] call.
 */
class SyncManager(
    private val engines: List<EntitySync>,
    private val networkMonitor: NetworkMonitor,
    private val clock: () -> Long = System::currentTimeMillis,
    private val config: SyncConfig = SyncConfig()
) {

    private val syncMutex = Mutex()
    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = _state.asStateFlow()

    /**
     * Starts observing connectivity in [scope]; whenever the device transitions
     * from offline to online a sync pass is enqueued. The returned [Job] can be
     * cancelled to stop the auto-sync loop (e.g. from `onStop`).
     */
    fun startAutoSync(scope: CoroutineScope): Job {
        val supervisor = SupervisorJob(scope.coroutineContext[Job])
        scope.launch(supervisor + Dispatchers.Default) {
            networkMonitor.isOnline
                .filter { online -> online }
                .collectLatest {
                    runCatching { syncNow() }
                }
        }
        return supervisor
    }

    /**
     * Runs a single sync pass. Returns a [SyncReport] describing the outcome.
     * If the device is offline this short-circuits with [SyncState.Offline]
     * and an empty report — callers do not need to check connectivity first.
     */
    suspend fun syncNow(): SyncReport {
        if (!networkMonitor.isCurrentlyOnline()) {
            _state.value = SyncState.Offline
            return SyncReport.Empty
        }
        return syncMutex.withLock {
            _state.value = SyncState.InProgress
            val report = runPass()
            _state.value = if (report.failed == 0) {
                SyncState.Success(clock(), report.pushed)
            } else {
                SyncState.Failed(
                    throwable = report.firstError ?: RuntimeException("Sync failed"),
                    remainingPending = report.remainingPending
                )
            }
            report
        }
    }

    private suspend fun runPass(): SyncReport {
        var pushed = 0
        var failed = 0
        val errors = mutableListOf<Throwable>()
        for (engine in engines) {
            val result = engine.sync(config)
            pushed += result.pushed
            failed += result.failed
            errors += result.errors
        }
        val remaining = engines.sumOf { it.pendingCount() }
        return SyncReport(pushed = pushed, failed = failed, remainingPending = remaining, errors = errors)
    }
}

/**
 * Tunables for a sync pass. Kept as a single data class so a future
 * remote-config or user-preferences layer can swap in different values without
 * disturbing call sites.
 */
data class SyncConfig(
    val conflictStrategy: ConflictStrategy = ConflictStrategy.SERVER_WINS,
    val maxAttemptsPerItem: Int = 3,
    val initialBackoffMillis: Long = 250L,
    val maxBackoffMillis: Long = 4_000L
)

/**
 * Boundary implemented per syncable table. Encapsulates iterating pending
 * rows, invoking the appropriate remote call, and reconciling the local
 * state on success or failure — the [SyncManager] treats each engine as an
 * opaque unit of work.
 */
interface EntitySync {
    suspend fun sync(config: SyncConfig): EntitySyncResult
    suspend fun pendingCount(): Int
}

data class EntitySyncResult(val pushed: Int, val failed: Int, val errors: List<Throwable>) {
    companion object {
        val Empty = EntitySyncResult(pushed = 0, failed = 0, errors = emptyList())
    }
}

/**
 * Runs [block] with exponential backoff. Retries are only attempted when the
 * failure looks transient (connectivity blips, timeouts, 5xx). Auth failures
 * (401), client errors, and cancellation propagate immediately since retrying
 * cannot make them succeed.
 */
internal suspend fun <T> retrying(config: SyncConfig, block: suspend () -> T): T {
    var attempt = 0
    var delayMs = config.initialBackoffMillis
    while (true) {
        try {
            return block()
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            attempt += 1
            if (attempt >= config.maxAttemptsPerItem || !t.isTransient()) throw t
            delay(delayMs)
            delayMs = (delayMs * 2).coerceAtMost(config.maxBackoffMillis)
        }
    }
}

internal fun Throwable.isTransient(): Boolean = when (this) {
    is NetworkException.NoConnectivity,
    is NetworkException.Timeout,
    is NetworkException.ServerError
    -> true
    else -> false
}
