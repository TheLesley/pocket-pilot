package com.example.pocketpilot

import android.app.Application
import androidx.work.Configuration
import com.example.pocketpilot.feature.finance.di.FinanceContainer
import com.example.pocketpilot.feature.finance.di.SyncWorkContainer
import com.example.pocketpilot.feature.notifications.di.NotificationsContainer
import com.example.pocketpilot.feature.security.di.SecurityContainer
import com.example.pocketpilot.feature.settings.di.SettingsContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry point. Owns the boot ordering that WorkManager quietly
 * depends on:
 *
 *  1. [FinanceContainer.init] materialises the Room database and, transitively,
 *     [com.example.pocketpilot.feature.finance.di.SyncContainer], so the
 *     [SyncManager] is ready before any worker can run.
 *  2. [NotificationsContainer.initWorkerFactory] primes the notification
 *     worker factory alongside the sync one so a single
 *     `DelegatingWorkerFactory` handed to `Configuration.Provider` covers
 *     both families.
 *  3. [SyncWorkContainer.init] then wires the sync scheduler and enqueues
 *     the periodic sync.
 *  4. [NotificationsContainer.init] wires the notification scheduler and
 *     enqueues the periodic budget-alert pass. The daily reminder is only
 *     scheduled when the user has it turned on.
 *
 * We implement [Configuration.Provider] instead of relying on WorkManager's
 * default initialiser so the combined [WorkerFactory] is picked up on the
 * very first `WorkManager.getInstance` call.
 */
class PocketPilotApplication :
    Application(),
    Configuration.Provider {

    // A tiny application-scoped scope so the reminder reschedule can react
    // to the very first preferences read without blocking `onCreate`.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        FinanceContainer.init(applicationContext)
        SettingsContainer.init(applicationContext)
        SecurityContainer.init(applicationContext)
        // Start the process-lifecycle observer before the first Activity can
        // begin its create/start cycle so the initial lock decision is made
        // in time for the root gate to render the lock screen instead of the
        // home screen on cold start.
        SecurityContainer.appLockManager.start()
        NotificationsContainer.initWorkerFactory(applicationContext)
        SyncWorkContainer.initWorkerFactory()
        SyncWorkContainer.init(applicationContext)
        NotificationsContainer.init(applicationContext)

        SyncWorkContainer.workScheduler.enqueuePeriodicSync()
        NotificationsContainer.workScheduler.enqueueBudgetAlerts()

        // Prime the daily reminder against the currently-persisted preferences
        // so a device reboot or app update does not silently drop the schedule.
        appScope.launch {
            val prefs = NotificationsContainer.preferencesRepository.get()
            NotificationsContainer.workScheduler.rescheduleDailyReminder(prefs)
        }
    }

    override val workManagerConfiguration: Configuration
        get() {
            // WorkManager can call this before `onCreate` in direct-boot
            // scenarios; ensure the finance graph and notification worker
            // factory are both up before we build the combined configuration.
            FinanceContainer.init(applicationContext)
            NotificationsContainer.initWorkerFactory(applicationContext)
            SyncWorkContainer.initWorkerFactory()
            return SyncWorkContainer.workConfiguration()
        }
}
