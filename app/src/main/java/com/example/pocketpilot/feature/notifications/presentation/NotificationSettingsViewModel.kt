package com.example.pocketpilot.feature.notifications.presentation

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.notifications.data.platform.NotificationPermission
import com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationDispatcher
import com.example.pocketpilot.feature.notifications.domain.usecase.ObserveNotificationPreferencesUseCase
import com.example.pocketpilot.feature.notifications.domain.usecase.UpdateNotificationPreferencesUseCase
import com.example.pocketpilot.feature.notifications.worker.NotificationWorkScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * UDF ViewModel for the notification settings screen. Owns:
 *  - the observation of persisted [NotificationPreferences],
 *  - the mutators that write back to DataStore,
 *  - side-effects for the daily-reminder scheduler,
 *  - the runtime POST_NOTIFICATIONS permission handshake.
 *
 * The permission is checked lazily (on `PermissionCheckRequested`) rather than
 * in `init` because the ViewModel outlives configuration changes; the Activity
 * pushes a fresh result each time the screen is composed.
 */
class NotificationSettingsViewModel(
    private val observePreferences: ObserveNotificationPreferencesUseCase,
    private val updatePreferences: UpdateNotificationPreferencesUseCase,
    private val workScheduler: NotificationWorkScheduler,
    private val dispatcher: NotificationDispatcher
) : BaseViewModel<NotificationSettingsState, NotificationSettingsEvent, NotificationSettingsEffect>(
    NotificationSettingsState(
        runtimePermissionRequired = NotificationPermission.isRuntimePermissionRequired
    )
) {

    private var observeJob: Job? = null

    init {
        observe()
        refreshPermissionState()
    }

    override fun handleEvent(event: NotificationSettingsEvent) {
        when (event) {
            NotificationSettingsEvent.Retry -> observe()
            NotificationSettingsEvent.PermissionCheckRequested -> {
                refreshPermissionState()
                if (currentState.runtimePermissionRequired && !currentState.notificationsAllowed) {
                    sendEffect(NotificationSettingsEffect.RequestPostNotificationsPermission)
                }
            }
            is NotificationSettingsEvent.PermissionResultReceived -> {
                setState { copy(notificationsAllowed = event.granted && dispatcher.areNotificationsAllowed()) }
                if (!event.granted) {
                    sendEffect(
                        NotificationSettingsEffect.ShowMessage(
                            "Notifications are disabled. You can enable them in system settings."
                        )
                    )
                }
            }

            is NotificationSettingsEvent.MasterToggled -> mutate { current ->
                updatePreferences.setMasterEnabled(event.enabled)
                workScheduler.rescheduleDailyReminder(current.copy(masterEnabled = event.enabled))
            }
            is NotificationSettingsEvent.BudgetAlertsToggled -> mutate {
                updatePreferences.setBudgetAlertsEnabled(event.enabled)
            }
            is NotificationSettingsEvent.BudgetWarningThresholdChanged -> mutate {
                updatePreferences.setBudgetWarningThresholdPercent(event.percent)
            }
            is NotificationSettingsEvent.DailyReminderToggled -> mutate { current ->
                updatePreferences.setDailyReminderEnabled(event.enabled)
                workScheduler.rescheduleDailyReminder(current.copy(dailyReminderEnabled = event.enabled))
            }
            is NotificationSettingsEvent.DailyReminderTimeChanged -> mutate { current ->
                updatePreferences.setDailyReminderTime(event.hourOfDay, event.minute)
                workScheduler.rescheduleDailyReminder(
                    current.copy(
                        dailyReminderHourOfDay = event.hourOfDay,
                        dailyReminderMinute = event.minute
                    )
                )
            }
        }
    }

    private fun refreshPermissionState() {
        val allowed = dispatcher.areNotificationsAllowed()
        setState { copy(notificationsAllowed = allowed) }
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = observePreferences()
            .onStart { setState { copy(preferences = UiState.Loading) } }
            .catch { t ->
                setState {
                    copy(
                        preferences = UiState.Error(
                            message = t.message ?: "Failed to load notification preferences",
                            throwable = t
                        )
                    )
                }
            }
            .onEach { prefs -> setState { copy(preferences = UiState.Success(prefs)) } }
            .launchIn(viewModelScope)
    }

    private fun mutate(block: suspend (NotificationPreferences) -> Unit) {
        val snapshot = (currentState.preferences as? UiState.Success)?.data
            ?: NotificationPreferences.Default
        viewModelScope.launch {
            runCatching { block(snapshot) }
                .onFailure { t ->
                    sendEffect(
                        NotificationSettingsEffect.ShowMessage(
                            t.message ?: "Failed to update notification setting"
                        )
                    )
                }
        }
    }
}
