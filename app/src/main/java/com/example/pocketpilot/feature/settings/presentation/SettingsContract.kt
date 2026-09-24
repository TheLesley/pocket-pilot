package com.example.pocketpilot.feature.settings.presentation

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.settings.domain.model.ThemeMode
import com.example.pocketpilot.feature.settings.domain.model.UserPreferences

@Immutable
data class SettingsState(
    val preferences: UiState<UserPreferences> = UiState.Loading,
    val isExporting: Boolean = false,
    val showClearConfirmation: Boolean = false
)

sealed interface SettingsEvent : UiEvent {
    data object Retry : SettingsEvent
    data class ThemeModeSelected(val mode: ThemeMode) : SettingsEvent
    data class CurrencySelected(val currencyCode: String) : SettingsEvent
    data class RecurringNotificationsToggled(val enabled: Boolean) : SettingsEvent
    data class BiometricLockToggled(val enabled: Boolean) : SettingsEvent

    data object ThemeSelectorClicked : SettingsEvent
    data object CurrencySelectorClicked : SettingsEvent
    data object DataManagementClicked : SettingsEvent
    data object NotificationSettingsClicked : SettingsEvent

    data object ExportDataClicked : SettingsEvent
    data object ClearDataClicked : SettingsEvent
    data object ClearDataConfirmed : SettingsEvent
    data object ClearDataDismissed : SettingsEvent
}

sealed interface SettingsEffect : UiEffect {
    data object NavigateToThemeSelector : SettingsEffect
    data object NavigateToCurrencySelector : SettingsEffect
    data object NavigateToDataManagement : SettingsEffect
    data object NavigateToNotificationSettings : SettingsEffect
    data class ShowMessage(val message: String) : SettingsEffect
}
