package com.example.pocketpilot.feature.settings.presentation.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.settings.domain.model.ThemeMode
import com.example.pocketpilot.feature.settings.presentation.SettingsEvent
import com.example.pocketpilot.feature.settings.presentation.SettingsSection
import com.example.pocketpilot.feature.settings.presentation.SettingsSelectableRow
import com.example.pocketpilot.feature.settings.presentation.SettingsViewModel
import com.example.pocketpilot.feature.settings.presentation.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorRoute(viewModel: SettingsViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentMode = (state.preferences as? UiState.Success)?.data?.themeMode

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentMode == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(PocketPilotTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
                ) {
                    SettingsSection(title = "Theme mode") {
                        Column {
                            ThemeMode.entries.forEach { mode ->
                                SettingsSelectableRow(
                                    title = mode.label,
                                    selected = mode == currentMode,
                                    onClick = {
                                        viewModel.onEvent(SettingsEvent.ThemeModeSelected(mode))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
