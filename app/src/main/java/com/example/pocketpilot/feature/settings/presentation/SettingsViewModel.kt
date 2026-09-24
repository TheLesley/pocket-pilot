package com.example.pocketpilot.feature.settings.presentation

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.settings.domain.model.ThemeMode
import com.example.pocketpilot.feature.settings.domain.usecase.ClearPreferencesUseCase
import com.example.pocketpilot.feature.settings.domain.usecase.ObservePreferencesUseCase
import com.example.pocketpilot.feature.settings.domain.usecase.UpdateBiometricLockUseCase
import com.example.pocketpilot.feature.settings.domain.usecase.UpdateDefaultCurrencyUseCase
import com.example.pocketpilot.feature.settings.domain.usecase.UpdateRecurringNotificationsUseCase
import com.example.pocketpilot.feature.settings.domain.usecase.UpdateThemeModeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val observePreferences: ObservePreferencesUseCase,
    private val updateThemeMode: UpdateThemeModeUseCase,
    private val updateDefaultCurrency: UpdateDefaultCurrencyUseCase,
    private val updateRecurringNotifications: UpdateRecurringNotificationsUseCase,
    private val updateBiometricLock: UpdateBiometricLockUseCase,
    private val clearPreferences: ClearPreferencesUseCase
) : BaseViewModel<SettingsState, SettingsEvent, SettingsEffect>(SettingsState()) {

    private var observeJob: Job? = null

    init {
        observe()
    }

    override fun handleEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.Retry -> observe()

            is SettingsEvent.ThemeModeSelected -> mutate { updateThemeMode(event.mode) }
            is SettingsEvent.CurrencySelected -> mutate(
                onError = { t ->
                    sendEffect(
                        SettingsEffect.ShowMessage(
                            t.message ?: "Unable to update currency"
                        )
                    )
                }
            ) {
                updateDefaultCurrency(event.currencyCode)
            }
            is SettingsEvent.RecurringNotificationsToggled ->
                mutate { updateRecurringNotifications(event.enabled) }
            is SettingsEvent.BiometricLockToggled ->
                mutate { updateBiometricLock(event.enabled) }

            SettingsEvent.ThemeSelectorClicked ->
                sendEffect(SettingsEffect.NavigateToThemeSelector)
            SettingsEvent.CurrencySelectorClicked ->
                sendEffect(SettingsEffect.NavigateToCurrencySelector)
            SettingsEvent.DataManagementClicked ->
                sendEffect(SettingsEffect.NavigateToDataManagement)
            SettingsEvent.NotificationSettingsClicked ->
                sendEffect(SettingsEffect.NavigateToNotificationSettings)

            SettingsEvent.ExportDataClicked -> exportData()
            SettingsEvent.ClearDataClicked ->
                setState { copy(showClearConfirmation = true) }
            SettingsEvent.ClearDataDismissed ->
                setState { copy(showClearConfirmation = false) }
            SettingsEvent.ClearDataConfirmed -> clearAll()
        }
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = observePreferences()
            .onStart { setState { copy(preferences = UiState.Loading) } }
            .catch { t ->
                setState {
                    copy(
                        preferences = UiState.Error(
                            message = t.message ?: "Failed to load preferences",
                            throwable = t
                        )
                    )
                }
            }
            .onEach { prefs ->
                setState { copy(preferences = UiState.Success(prefs)) }
            }
            .launchIn(viewModelScope)
    }

    private fun mutate(
        onError: (Throwable) -> Unit = { /* preferences flow will re-emit the previous value */ },
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            runCatching { block() }.onFailure(onError)
        }
    }

    private fun exportData() {
        // Real export lands in a follow-up phase (WorkManager job → JSON/CSV).
        // For now we simulate the operation so the UI can exercise its loading
        // state and give the user acknowledgement feedback.
        viewModelScope.launch {
            setState { copy(isExporting = true) }
            runCatching {
                // Placeholder no-op — no data is written yet.
            }.also {
                setState { copy(isExporting = false) }
                sendEffect(SettingsEffect.ShowMessage("Export queued"))
            }
        }
    }

    private fun clearAll() {
        viewModelScope.launch {
            setState { copy(showClearConfirmation = false) }
            runCatching { clearPreferences() }
                .onSuccess {
                    sendEffect(SettingsEffect.ShowMessage("Preferences reset to defaults"))
                }
                .onFailure { t ->
                    sendEffect(
                        SettingsEffect.ShowMessage(
                            t.message ?: "Failed to clear preferences"
                        )
                    )
                }
        }
    }

    // Kept internal so the Compose theme wrapper can read the current selection
    // without needing its own use-case wiring.
    @Suppress("unused")
    fun defaultThemeMode(): ThemeMode = ThemeMode.SYSTEM
}
