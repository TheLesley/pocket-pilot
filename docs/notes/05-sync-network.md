# 05 – Offline-first sync & networking

Files: `core/sync/*` (SyncManager, NetworkMonitor, SyncWorker, SyncWorkScheduler, SyncWorkerFactory, ConflictStrategy), `feature/finance/data/sync/*SyncEngine.kt`, `feature/finance/di/SyncContainer.kt`, `core/network/*`

> Reality check: there is **no live server**. `NetworkConfig.remoteBackendUrl = null`; Retrofit points at a placeholder `https://api.pocketpilot.app/`. All of this is built and structured correctly, but in the running app every sync attempt fails. Say so honestly if asked: "The sync layer is designed and wired against a REST contract; the backend isn't built yet."

## The idea
Each syncable row has a `sync_status`: `SYNCED`, `PENDING_INSERT`, `PENDING_UPDATE`, `PENDING_DELETE` (see note 02). Sync = "find everything not SYNCED, send it to the server, then mark it SYNCED".

## Pieces
| Piece | Job |
|---|---|
| `EntitySync` (interface) | One per table: `sync(config)` and `pendingCount()` |
| `TransactionSyncEngine` (+ Budget, SavingsGoal) | Pushes inserts, then updates, then deletes; records errors per row |
| `SyncManager` | Runs all engines in one pass; exposes `state: StateFlow<SyncState>` |
| `NetworkMonitor` | `Flow<Boolean>` of online/offline using `ConnectivityManager` |
| `SyncWorker` / `SyncWorkScheduler` | WorkManager jobs: every 15 min + on-demand "Sync now" |
| `safeApiCall` + `NetworkException` | Turns raw exceptions into typed errors |

## SyncManager in detail
- `syncNow()`: if offline → state `Offline`, return. Otherwise lock a **`Mutex`** (so an automatic sync and a manual "Sync now" can't run at the same time), run every engine, then set `Success` or `Failed`.
- `startAutoSync(scope)`: collects `networkMonitor.isOnline`, filters to `true`, and calls `syncNow()` each time the phone comes back online. Called from `MainActivity` with `lifecycleScope`.
- Adding a new syncable table = add another `EntitySync` to the list. The manager doesn't know about specific tables (open/closed principle).

## Retry with exponential backoff (`retrying` in SyncManager.kt)
Try → on failure wait 250 ms → 500 → 1000 … up to 4 s, max 3 attempts – but **only for transient errors** (no connectivity, timeout, 5xx). A 401 or other 4xx is thrown immediately because retrying can't fix it. `CancellationException` is always rethrown – swallowing it would break coroutine cancellation.

## Conflicts
If the server rejects a create/update with a 4xx, the engine fetches the server's copy and applies `ConflictStrategy`:
- `SERVER_WINS` (default) – overwrite local with the server's version.
- `CLIENT_TIMESTAMP` – last write wins: keep whichever was updated most recently.

## WorkManager
- `enqueuePeriodicSync()` – `PeriodicWorkRequest` every 15 min (WorkManager's minimum), constraint `NetworkType.CONNECTED`, exponential backoff, `ExistingPeriodicWorkPolicy.KEEP` (don't duplicate if already scheduled).
- `enqueueOneTimeSync()` – for the "Sync now" button.
- `BaseSyncWorker` is a `CoroutineWorker`; returns `Result.success`, `Result.retry()` (WorkManager will back off and retry) or `Result.failure` after max attempts.
- Workers need the `SyncManager`, so a custom **`WorkerFactory`** creates them; that's why the Application implements `Configuration.Provider`.

## NetworkMonitor uses `callbackFlow`
Android's `ConnectivityManager` reports changes through callbacks (`onAvailable`, `onLost`, `onCapabilitiesChanged`). `callbackFlow { ... awaitClose { unregister } }` turns those callbacks into a `Flow`, and unregisters automatically when nobody is collecting. It checks `NET_CAPABILITY_VALIDATED` so "connected to Wi-Fi with no internet" doesn't count as online.

## Network layer (`core/network`)
OkHttp with timeouts (15 s connect, 20 s read/write, 30 s call), an `AuthHeaderInterceptor` that adds the token header, a logging interceptor, and Retrofit with the kotlinx.serialization converter. `safeApiCall` maps: timeout → `Timeout`, no host/IO → `NoConnectivity`, bad JSON → `Serialization`, HTTP 401 → `Unauthorized`, 4xx → `ClientError`, 5xx → `ServerError`.

## Questions
1. Walk me through what happens when the phone reconnects to the internet.
<details><summary>Show answer</summary>NetworkMonitor's Flow emits true → SyncManager.startAutoSync calls syncNow → Mutex locked → each engine pushes pending inserts, updates, deletes → rows marked SYNCED or errors recorded → SyncState updated.</details>

2. Why the Mutex?
<details><summary>Show answer</summary>To stop two sync passes (automatic and manual, or worker and app) running at once and uploading the same rows twice.</details>

3. Which errors are retried and why not the others?
<details><summary>Show answer</summary>Only transient ones – no connectivity, timeouts, 5xx. 4xx/401 mean the request itself is wrong or unauthorised, so retrying won't help.</details>

4. What's `callbackFlow` and why `awaitClose`?
<details><summary>Show answer</summary>A builder that turns callback APIs into a Flow. `awaitClose` keeps it open until collection stops, then runs cleanup (unregistering the callback) to avoid leaks.</details>

5. Why must you rethrow `CancellationException`?
<details><summary>Show answer</summary>Coroutines use it to cancel cooperatively; catching it would keep a cancelled coroutine running.</details>

6. What does `ExistingPeriodicWorkPolicy.KEEP` do?
<details><summary>Show answer</summary>If a periodic job with that unique name already exists, keep it instead of scheduling a duplicate or resetting its timer.</details>
