package com.example.pocketpilot.feature.settings.presentation.data

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.PocketPilotPrimaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotSecondaryButton
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.feature.settings.presentation.SettingsEffect
import com.example.pocketpilot.feature.settings.presentation.SettingsEvent
import com.example.pocketpilot.feature.settings.presentation.SettingsSection
import com.example.pocketpilot.feature.settings.presentation.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementRoute(viewModel: SettingsViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            if (effect is SettingsEffect.ShowMessage) {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PocketPilotTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
            ) {
                SettingsSection(title = "Export") {
                    Column(
                        modifier = Modifier.padding(PocketPilotTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
                    ) {
                        Text(
                            text = "Bundle your transactions, budgets and savings goals " +
                                "into a shareable file.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        PocketPilotPrimaryButton(
                            text = if (state.isExporting) "Exporting…" else "Export data",
                            onClick = { viewModel.onEvent(SettingsEvent.ExportDataClicked) },
                            loading = state.isExporting,
                            leadingIcon = Icons.Default.Share,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                SettingsSection(title = "Reset") {
                    Column(
                        modifier = Modifier.padding(PocketPilotTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
                    ) {
                        Text(
                            text = "Restore theme, currency, notification and lock " +
                                "preferences to their defaults. Your finance data is not " +
                                "touched.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        PocketPilotSecondaryButton(
                            text = "Reset preferences",
                            onClick = { viewModel.onEvent(SettingsEvent.ClearDataClicked) },
                            leadingIcon = Icons.Default.Delete,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (state.showClearConfirmation) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(SettingsEvent.ClearDataDismissed) },
                    title = { Text("Reset preferences?") },
                    text = {
                        Text(
                            "This will restore theme, currency, notification and lock " +
                                "settings to their defaults."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.onEvent(SettingsEvent.ClearDataConfirmed) }
                        ) { Text("Reset") }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.onEvent(SettingsEvent.ClearDataDismissed) }
                        ) { Text("Cancel") }
                    }
                )
            }
        }
    }
}
