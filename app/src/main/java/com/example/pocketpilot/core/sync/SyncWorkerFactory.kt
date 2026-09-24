package com.example.pocketpilot.core.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

/**
 * Constructor-injects the shared [SyncManager] into workers. WorkManager will
 * otherwise instantiate workers reflectively via a no-arg constructor, which
 * defeats our attempt to keep sync engines behind a single service-locator
 * ([SyncContainer]). Registered on the [androidx.work.Configuration] built by
 * `PocketPilotApplication`.
 *
 * Returning `null` for unknown worker names is intentional: it lets
 * WorkManager fall back to its default factory so any future workers that do
 * not need the sync graph still resolve.
 */
class SyncWorkerFactory(private val syncManager: SyncManager) : WorkerFactory() {

    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
        when (workerClassName) {
            OneTimeSyncWorker::class.java.name ->
                OneTimeSyncWorker(appContext, workerParameters, syncManager)
            PeriodicSyncWorker::class.java.name ->
                PeriodicSyncWorker(appContext, workerParameters, syncManager)
            else -> null
        }
}
