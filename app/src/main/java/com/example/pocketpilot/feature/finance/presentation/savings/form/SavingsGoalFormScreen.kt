package com.example.pocketpilot.feature.finance.presentation.savings.form

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.ErrorState
import com.example.pocketpilot.core.designsystem.component.PocketPilotPrimaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotSecondaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotTextField
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.feature.finance.presentation.util.DateFormatter

@Composable
fun SavingsGoalFormRoute(viewModel: SavingsGoalFormViewModel, onSaved: () -> Unit, onCancelled: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SavingsGoalFormEffect.Saved -> onSaved()
                SavingsGoalFormEffect.Cancelled -> onCancelled()
                is SavingsGoalFormEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    SavingsGoalFormScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalFormScreen(state: SavingsGoalFormState, snackbarHostState: SnackbarHostState, onEvent: (SavingsGoalFormEvent) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEdit) "Edit savings goal" else "New savings goal") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SavingsGoalFormEvent.Cancel) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            when {
                state.isLoadingExisting -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                state.loadError != null -> ErrorState(
                    message = state.loadError,
                    onRetry = { onEvent(SavingsGoalFormEvent.Retry) }
                )
                else -> FormBody(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun FormBody(state: SavingsGoalFormState, onEvent: (SavingsGoalFormEvent) -> Unit) {
    var showTargetPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        PocketPilotTextField(
            value = state.name,
            onValueChange = { onEvent(SavingsGoalFormEvent.NameChanged(it)) },
            label = "Name",
            placeholder = "e.g. Emergency fund",
            errorMessage = state.nameError,
            enabled = !state.isSubmitting
        )

        PocketPilotTextField(
            value = state.targetInput,
            onValueChange = { onEvent(SavingsGoalFormEvent.TargetChanged(it)) },
            label = "Target (${state.currencyCode})",
            keyboardType = KeyboardType.Decimal,
            errorMessage = state.targetError,
            enabled = !state.isSubmitting
        )

        DateField(
            label = "Target date",
            millis = state.targetDateMillis,
            onClick = { showTargetPicker = true },
            onClear = { onEvent(SavingsGoalFormEvent.TargetDateChanged(null)) },
            enabled = !state.isSubmitting
        )
        if (state.targetDateError != null) {
            Text(
                text = state.targetDateError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        PocketPilotTextField(
            value = state.note,
            onValueChange = { onEvent(SavingsGoalFormEvent.NoteChanged(it)) },
            label = "Note",
            placeholder = "Optional",
            singleLine = false,
            errorMessage = state.noteError,
            enabled = !state.isSubmitting
        )

        if (state.submitError != null) {
            Text(
                text = state.submitError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
        ) {
            PocketPilotSecondaryButton(
                text = "Cancel",
                onClick = { onEvent(SavingsGoalFormEvent.Cancel) },
                enabled = !state.isSubmitting,
                modifier = Modifier.weight(1f)
            )
            PocketPilotPrimaryButton(
                text = if (state.isEdit) "Save" else "Add",
                onClick = { onEvent(SavingsGoalFormEvent.Submit) },
                enabled = state.canSubmit,
                loading = state.isSubmitting,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showTargetPicker) {
        DatePickerModal(
            initialMillis = state.targetDateMillis ?: System.currentTimeMillis(),
            onConfirm = {
                onEvent(SavingsGoalFormEvent.TargetDateChanged(it))
                showTargetPicker = false
            },
            onDismiss = { showTargetPicker = false }
        )
    }
}

@Composable
private fun DateField(label: String, millis: Long?, onClick: () -> Unit, enabled: Boolean, onClear: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = millis?.let { DateFormatter.formatFull(it) } ?: "No deadline",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (onClear != null && millis != null) {
                TextButton(onClick = onClear, enabled = enabled) { Text("Clear") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(initialMillis: Long, onConfirm: (Long) -> Unit, onDismiss: () -> Unit) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let(onConfirm) ?: onDismiss()
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = pickerState)
    }
}
