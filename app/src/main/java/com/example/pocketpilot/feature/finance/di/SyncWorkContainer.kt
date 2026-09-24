package com.example.pocketpilot.feature.finance.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import androidx.work.WorkManager
import com.example.pocketpilot.core.sync.SyncWorkScheduler
import com.example.pocketpilot.core.sync.SyncWorkerFactory
import com.example.pocketpilot.feature.notifications.di.NotificationsContainer

/**
 * Companion to [SyncContainer] that assembles the WorkManager-facing side of
 * the sync graph: the [SyncWorkerFactory] used by the app's
 * [androidx.work.Configuration], and the [SyncWorkScheduler] that ViewModels
 * and the Application class use to enqueue work.
 *
 * Kept separate from [SyncContainer] so pure unit tests can spin up the sync
 * engines without dragging in the WorkManager runtime.
 */
object SyncWorkContainer {

    @Volatile
    private var workerFactory: SyncWorkerFactory? = null

    @Volatile
    private var scheduler: SyncWorkScheduler? = null

    /**
     * Builds the [androidx.work.Configuration] that `PocketPilotApplication`
     * hands back from [androidx.work.Configuration.Provider.workManagerConfiguration].
     * Must run before [WorkManager.getInstance] is ever called — WorkManager
     * caches the configuration on first access.
     */
    fun workConfiguration(): Configuration {
        val factory = requireWorkerFactory()
        val delegating = DelegatingWorkerFactory().apply {
            addFactory(factory)
            // The notification workers live in a separate feature, but their
            // factory has to be registered on the same `Configuration` — WorkManager
            // caches exactly one on first access, so we combine here rather
            // than trying to swap it later.
            runCatching { NotificationsContainer.workerFactory }
                .getOrNull()
                ?.let(::addFactory)
        }
        return Configuration.Builder()
            .setWorkerFactory(delegating)
            .build()
    }

    fun init(context: Context) {
        if (scheduler != null) return
        synchronized(this) {
            if (scheduler != null) return
            workerFactory = SyncWorkerFactory(SyncContainer.manager)
            scheduler = SyncWorkScheduler(WorkManager.getInstance(context.applicationContext))
        }
    }

    /**
     * Called from `PocketPilotApplication.workManagerConfiguration` before the
     * scheduler is wired up. Only the [SyncWorkerFactory] is needed at that
     * point, so this initialiser avoids touching [WorkManager] (which would
     * recurse into `workManagerConfiguration`).
     */
    fun initWorkerFactory() {
        if (workerFactory != null) return
        synchronized(this) {
            if (workerFactory != null) return
            workerFactory = SyncWorkerFactory(SyncContainer.manager)
        }
    }

    val workScheduler: SyncWorkScheduler
        get() = checkNotNull(scheduler) {
            "SyncWorkContainer.init(context) must be called before accessing the scheduler."
        }

    private fun requireWorkerFactory(): SyncWorkerFactory = checkNotNull(workerFactory) {
        "SyncWorkContainer.initWorkerFactory() must be called before building work configuration."
    }
}
