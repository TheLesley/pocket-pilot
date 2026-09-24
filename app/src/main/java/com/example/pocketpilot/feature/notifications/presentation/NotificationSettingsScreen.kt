package com.example.pocketpilot.feature.notifications.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences

@Composable
fun NotificationSettingsRoute(viewModel: NotificationSettingsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Runtime POST_NOTIFICATIONS permission wiring. The launcher only makes
    // sense on API 33+, but Android's own contract handles older releases by
    // returning true immediately, so we can register unconditionally.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onEvent(NotificationSettingsEvent.PermissionResultReceived(granted))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                NotificationSettingsEffect.RequestPostNotificationsPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                is NotificationSettingsEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    // Re-check on every entry so returning from system settings refreshes the banner.
    LaunchedEffect(Unit) {
        viewModel.onEvent(NotificationSettingsEvent.PermissionCheckRequested)
    }

    NotificationSettingsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    state: NotificationSettingsState,
    onEvent: (NotificationSettingsEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Notifications") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val prefs = state.preferences) {
                UiState.Idle, UiState.Loading -> LoadingIndicator()
                UiState.Empty -> Content(
                    state = state,
                    preferences = NotificationPreferences.Default,
                    onEvent = onEvent
                )
                is UiState.Success -> Content(
                    state = state,
                    preferences = prefs.data,
                    onEvent = onEvent
                )
                is UiState.Error -> ErrorState(
                    message = prefs.message,
                    onRetry = { onEvent(NotificationSettingsEvent.Retry) }
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
private fun Content(state: NotificationSettingsState, preferences: NotificationPreferences, onEvent: (NotificationSettingsEvent) -> Unit) {
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
        if (!state.notificationsAllowed) {
            PermissionBanner(
                onGrantClick = { onEvent(NotificationSettingsEvent.PermissionCheckRequested) }
            )
        }

        SettingsGroup(title = "General") {
            SwitchRow(
                title = "All notifications",
                subtitle = "Master switch for every alert",
                checked = preferences.masterEnabled,
                onCheckedChange = { onEvent(NotificationSettingsEvent.MasterToggled(it)) }
            )
        }

        SettingsGroup(title = "Budget alerts") {
            SwitchRow(
                title = "Budget threshold alerts",
                subtitle = "Warn me when I'm close to a budget limit",
                checked = preferences.budgetAlertsEnabled,
                enabled = preferences.masterEnabled,
                onCheckedChange = {
                    onEvent(NotificationSettingsEvent.BudgetAlertsToggled(it))
                }
            )
            ThresholdSliderRow(
                percent = preferences.budgetWarningThresholdPercent,
                enabled = preferences.masterEnabled && preferences.budgetAlertsEnabled,
                onPercentChangeFinished = {
                    onEvent(NotificationSettingsEvent.BudgetWarningThresholdChanged(it))
                }
            )
        }

        SettingsGroup(title = "Daily reminders") {
            SwitchRow(
                title = "Daily transaction reminder",
                subtitle = reminderSubtitle(preferences),
                checked = preferences.dailyReminderEnabled,
                enabled = preferences.masterEnabled,
                onCheckedChange = {
                    onEvent(NotificationSettingsEvent.DailyReminderToggled(it))
                }
            )
        }
    }
}

@Composable
private fun PermissionBanner(onGrantClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PocketPilotTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notifications are disabled",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "PocketPilot needs permission to post alerts and reminders.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Grant",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(start = PocketPilotTheme.spacing.sm)
                    .clickable(onClick = onGrantClick)
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
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
        Switch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ThresholdSliderRow(percent: Int, enabled: Boolean, onPercentChangeFinished: (Int) -> Unit) {
    val localState = androidx.compose.runtime.remember(percent) {
        androidx.compose.runtime.mutableFloatStateOf(percent.toFloat())
    }
    val localValue = localState.floatValue
    Column(
        modifier = Modifier.padding(
            horizontal = PocketPilotTheme.spacing.md,
            vertical = PocketPilotTheme.spacing.sm
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Warn at",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${localValue.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Slider(
            value = localValue,
            onValueChange = { localState.floatValue = it },
            valueRange = 50f..100f,
            steps = 4,
            enabled = enabled,
            onValueChangeFinished = { onPercentChangeFinished(localState.floatValue.toInt()) }
        )
    }
}

private fun reminderSubtitle(preferences: NotificationPreferences): String {
    val h = preferences.dailyReminderHourOfDay.toString().padStart(2, '0')
    val m = preferences.dailyReminderMinute.toString().padStart(2, '0')
    return "Delivered daily around $h:$m"
}
