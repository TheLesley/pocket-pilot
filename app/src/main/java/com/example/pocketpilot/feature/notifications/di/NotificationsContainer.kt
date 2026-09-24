package com.example.pocketpilot.feature.notifications.di

import android.content.Context
import androidx.work.WorkManager
import com.example.pocketpilot.feature.finance.di.BudgetContainer
import com.example.pocketpilot.feature.notifications.data.local.NotificationPreferencesDataSource
import com.example.pocketpilot.feature.notifications.data.platform.AndroidNotificationDispatcher
import com.example.pocketpilot.feature.notifications.data.repository.NotificationPreferencesRepositoryImpl
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationDispatcher
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationPreferencesRepository
import com.example.pocketpilot.feature.notifications.domain.usecase.BuildDailyReminderUseCase
import com.example.pocketpilot.feature.notifications.domain.usecase.EvaluateBudgetAlertsUseCase
import com.example.pocketpilot.feature.notifications.domain.usecase.ObserveNotificationPreferencesUseCase
import com.example.pocketpilot.feature.notifications.domain.usecase.UpdateNotificationPreferencesUseCase
import com.example.pocketpilot.feature.notifications.worker.NotificationWorkScheduler
import com.example.pocketpilot.feature.notifications.worker.NotificationWorkerFactory

/**
 * Service locator for the notifications feature. Mirrors the shape of
 * [com.example.pocketpilot.feature.settings.di.SettingsContainer] and
 * [com.example.pocketpilot.feature.finance.di.SyncWorkContainer]:
 * `init(context)` must be called from the Application before anything else
 * touches [dataSource] or [workScheduler].
 *
 * The [workerFactory] is exposed so `PocketPilotApplication` can hand it to
 * WorkManager's `Configuration.Provider` alongside the sync worker factory.
 */
object NotificationsContainer {

    @Volatile
    private var dataSource: NotificationPreferencesDataSource? = null

    @Volatile
    private var scheduler: NotificationWorkScheduler? = null

    @Volatile
    private var dispatcherInstance: NotificationDispatcher? = null

    @Volatile
    private var workerFactoryInstance: NotificationWorkerFactory? = null

    fun init(context: Context) {
        if (dataSource != null && scheduler != null) return
        synchronized(this) {
            if (dataSource == null) {
                dataSource = NotificationPreferencesDataSource.create(context.applicationContext)
            }
            if (dispatcherInstance == null) {
                dispatcherInstance = AndroidNotificationDispatcher(
                    context = context.applicationContext,
                    preferencesRepository = preferencesRepository
                )
            }
            if (scheduler == null) {
                scheduler = NotificationWorkScheduler(
                    WorkManager.getInstance(context.applicationContext)
                )
            }
        }
    }

    /**
     * Materialises the [NotificationWorkerFactory] without touching
     * [WorkManager]. Called from `Application.workManagerConfiguration`,
     * which runs before the periodic-work scheduler is wired up.
     */
    fun initWorkerFactory(context: Context) {
        if (workerFactoryInstance != null) return
        synchronized(this) {
            if (workerFactoryInstance != null) return
            if (dataSource == null) {
                dataSource = NotificationPreferencesDataSource.create(context.applicationContext)
            }
            if (dispatcherInstance == null) {
                dispatcherInstance = AndroidNotificationDispatcher(
                    context = context.applicationContext,
                    preferencesRepository = preferencesRepository
                )
            }
            workerFactoryInstance = NotificationWorkerFactory(
                observeBudgetsProgress = { BudgetContainer.observeBudgetsProgressUseCase() },
                preferencesRepository = preferencesRepository,
                evaluateBudgetAlerts = evaluateBudgetAlerts,
                buildDailyReminder = buildDailyReminder,
                dispatcher = dispatcher
            )
        }
    }

    private fun requireDataSource(): NotificationPreferencesDataSource = checkNotNull(dataSource) {
        "NotificationsContainer.init(context) must be called before accessing preferences."
    }

    val preferencesRepository: NotificationPreferencesRepository by lazy {
        NotificationPreferencesRepositoryImpl(requireDataSource())
    }

    val observePreferences: ObserveNotificationPreferencesUseCase by lazy {
        ObserveNotificationPreferencesUseCase(preferencesRepository)
    }

    val updatePreferences: UpdateNotificationPreferencesUseCase by lazy {
        UpdateNotificationPreferencesUseCase(preferencesRepository)
    }

    val evaluateBudgetAlerts: EvaluateBudgetAlertsUseCase by lazy {
        EvaluateBudgetAlertsUseCase()
    }

    val buildDailyReminder: BuildDailyReminderUseCase by lazy {
        BuildDailyReminderUseCase()
    }

    val dispatcher: NotificationDispatcher
        get() = checkNotNull(dispatcherInstance) {
            "NotificationsContainer.init(context) must be called before accessing the dispatcher."
        }

    val workScheduler: NotificationWorkScheduler
        get() = checkNotNull(scheduler) {
            "NotificationsContainer.init(context) must be called before accessing the scheduler."
        }

    val workerFactory: NotificationWorkerFactory
        get() = checkNotNull(workerFactoryInstance) {
            "NotificationsContainer.initWorkerFactory(context) must be called before requesting the factory."
        }
}
