package com.example.pocketpilot.feature.settings.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.ErrorState
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.settings.domain.model.ThemeMode
import com.example.pocketpilot.feature.settings.domain.model.UserPreferences

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onNavigateToThemeSelector: () -> Unit,
    onNavigateToCurrencySelector: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsEffect.NavigateToThemeSelector -> onNavigateToThemeSelector()
                SettingsEffect.NavigateToCurrencySelector -> onNavigateToCurrencySelector()
                SettingsEffect.NavigateToDataManagement -> onNavigateToDataManagement()
                SettingsEffect.NavigateToNotificationSettings -> onNavigateToNotificationSettings()
                is SettingsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    SettingsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val prefs = state.preferences) {
                UiState.Idle,
                UiState.Loading -> LoadingIndicator()

                UiState.Empty -> SettingsContent(
                    preferences = UserPreferences.Default,
                    onEvent = onEvent
                )

                is UiState.Success -> SettingsContent(
                    preferences = prefs.data,
                    onEvent = onEvent
                )

                is UiState.Error -> ErrorState(
                    message = prefs.message,
                    onRetry = { onEvent(SettingsEvent.Retry) }
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SettingsContent(preferences: UserPreferences, onEvent: (SettingsEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = PocketPilotTheme.spacing.md,
                vertical = PocketPilotTheme.spacing.md
            ),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        SettingsSection(title = "Appearance") {
            SettingsNavRow(
                title = "Theme",
                value = preferences.themeMode.label,
                onClick = { onEvent(SettingsEvent.ThemeSelectorClicked) }
            )
        }

        SettingsSection(title = "Preferences") {
            SettingsNavRow(
                title = "Default currency",
                value = preferences.defaultCurrencyCode,
                onClick = { onEvent(SettingsEvent.CurrencySelectorClicked) }
            )
        }

        SettingsSection(title = "Notifications") {
            SettingsSwitchRow(
                title = "Recurring transaction reminders",
                subtitle = "Notify me before recurring items post",
                checked = preferences.recurringNotificationsEnabled,
                onCheckedChange = {
                    onEvent(SettingsEvent.RecurringNotificationsToggled(it))
                }
            )
            SettingsNavRow(
                title = "Alerts & reminders",
                value = "Budget, daily",
                onClick = { onEvent(SettingsEvent.NotificationSettingsClicked) }
            )
        }

        SettingsSection(title = "Security") {
            SettingsSwitchRow(
                title = "Biometric app lock",
                subtitle = "Require fingerprint or face unlock on launch",
                checked = preferences.biometricLockEnabled,
                onCheckedChange = {
                    onEvent(SettingsEvent.BiometricLockToggled(it))
                }
            )
        }

        SettingsSection(title = "Data") {
            SettingsNavRow(
                title = "Data management",
                value = "Export, reset",
                onClick = { onEvent(SettingsEvent.DataManagementClicked) }
            )
        }
    }
}

@Composable
internal fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = PocketPilotTheme.extendedColors.border,
                    shape = MaterialTheme.shapes.large,
                ),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            content()
        }
    }
}

@Composable
internal fun SettingsNavRow(title: String, value: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = PocketPilotTheme.spacing.md,
                vertical = PocketPilotTheme.spacing.md
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
internal fun SettingsSwitchRow(title: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = PocketPilotTheme.spacing.md,
                vertical = PocketPilotTheme.spacing.md
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun SettingsSelectableRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = PocketPilotTheme.spacing.md,
                vertical = PocketPilotTheme.spacing.md
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

internal val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "System default"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }
