package com.example.pocketpilot.feature.finance.presentation.budget.form

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
import androidx.compose.material3.FilterChip
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
import com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod
import com.example.pocketpilot.feature.finance.presentation.util.DateFormatter

@Composable
fun BudgetFormRoute(viewModel: BudgetFormViewModel, onSaved: () -> Unit, onCancelled: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                BudgetFormEffect.Saved -> onSaved()
                BudgetFormEffect.Cancelled -> onCancelled()
                is BudgetFormEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    BudgetFormScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetFormScreen(state: BudgetFormState, snackbarHostState: SnackbarHostState, onEvent: (BudgetFormEvent) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEdit) "Edit budget" else "New budget") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(BudgetFormEvent.Cancel) }) {
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
                state.isLoadingExisting -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.loadError != null -> ErrorState(
                    message = state.loadError,
                    onRetry = { onEvent(BudgetFormEvent.Retry) }
                )
                else -> FormBody(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun FormBody(state: BudgetFormState, onEvent: (BudgetFormEvent) -> Unit) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        PocketPilotTextField(
            value = state.name,
            onValueChange = { onEvent(BudgetFormEvent.NameChanged(it)) },
            label = "Name",
            placeholder = "e.g. Groceries",
            errorMessage = state.nameError,
            enabled = !state.isSubmitting
        )

        PocketPilotTextField(
            value = state.limitInput,
            onValueChange = { onEvent(BudgetFormEvent.LimitChanged(it)) },
            label = "Limit (${state.currencyCode})",
            keyboardType = KeyboardType.Decimal,
            errorMessage = state.limitError,
            enabled = !state.isSubmitting
        )

        PeriodSelector(
            selected = state.period,
            onSelect = { onEvent(BudgetFormEvent.PeriodChanged(it)) },
            enabled = !state.isSubmitting
        )

        PocketPilotTextField(
            value = state.category,
            onValueChange = { onEvent(BudgetFormEvent.CategoryChanged(it)) },
            label = "Category",
            placeholder = "e.g. Food",
            supportingText = "Optional — links matching transactions automatically",
            errorMessage = state.categoryError,
            enabled = !state.isSubmitting
        )

        DateField(
            label = "Start date",
            millis = state.startsAtMillis,
            onClick = { showStartPicker = true },
            enabled = !state.isSubmitting
        )
        DateField(
            label = "End date",
            millis = state.endsAtMillis,
            onClick = { showEndPicker = true },
            onClear = { onEvent(BudgetFormEvent.EndDateChanged(null)) },
            enabled = !state.isSubmitting
        )
        if (state.dateError != null) {
            Text(
                text = state.dateError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

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
                onClick = { onEvent(BudgetFormEvent.Cancel) },
                enabled = !state.isSubmitting,
                modifier = Modifier.weight(1f)
            )
            PocketPilotPrimaryButton(
                text = if (state.isEdit) "Save" else "Add",
                onClick = { onEvent(BudgetFormEvent.Submit) },
                enabled = state.canSubmit,
                loading = state.isSubmitting,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showStartPicker) {
        DatePickerModal(
            initialMillis = state.startsAtMillis,
            onConfirm = {
                onEvent(BudgetFormEvent.StartDateChanged(it))
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }
    if (showEndPicker) {
        DatePickerModal(
            initialMillis = state.endsAtMillis ?: state.startsAtMillis,
            onConfirm = {
                onEvent(BudgetFormEvent.EndDateChanged(it))
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

@Composable
private fun PeriodSelector(selected: BudgetPeriod, onSelect: (BudgetPeriod) -> Unit, enabled: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)) {
        Text(
            text = "Period",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)) {
            BudgetPeriod.entries.forEach { candidate ->
                FilterChip(
                    selected = candidate == selected,
                    onClick = { onSelect(candidate) },
                    label = { Text(candidate.label()) },
                    enabled = enabled
                )
            }
        }
    }
}

private fun BudgetPeriod.label(): String = when (this) {
    BudgetPeriod.WEEKLY -> "Weekly"
    BudgetPeriod.MONTHLY -> "Monthly"
    BudgetPeriod.YEARLY -> "Yearly"
    BudgetPeriod.CUSTOM -> "Custom"
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
                    text = millis?.let { DateFormatter.formatFull(it) } ?: "Not set",
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
