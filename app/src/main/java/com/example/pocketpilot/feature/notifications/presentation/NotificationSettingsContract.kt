package com.example.pocketpilot.feature.notifications.presentation

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences

@Immutable
data class NotificationSettingsState(
    val preferences: UiState<NotificationPreferences> = UiState.Loading,
    val notificationsAllowed: Boolean = true,
    val runtimePermissionRequired: Boolean = false
)

sealed interface NotificationSettingsEvent : UiEvent {
    data object Retry : NotificationSettingsEvent
    data object PermissionCheckRequested : NotificationSettingsEvent
    data class PermissionResultReceived(val granted: Boolean) : NotificationSettingsEvent

    data class MasterToggled(val enabled: Boolean) : NotificationSettingsEvent
    data class BudgetAlertsToggled(val enabled: Boolean) : NotificationSettingsEvent
    data class BudgetWarningThresholdChanged(val percent: Int) : NotificationSettingsEvent
    data class DailyReminderToggled(val enabled: Boolean) : NotificationSettingsEvent
    data class DailyReminderTimeChanged(val hourOfDay: Int, val minute: Int) : NotificationSettingsEvent
}

sealed interface NotificationSettingsEffect : UiEffect {
    /** UI should launch the runtime POST_NOTIFICATIONS permission request. */
    data object RequestPostNotificationsPermission : NotificationSettingsEffect
    data class ShowMessage(val message: String) : NotificationSettingsEffect
}
