package com.example.pocketpilot.feature.finance.presentation.form

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
import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.feature.finance.presentation.util.DateFormatter

@Composable
fun TransactionFormRoute(viewModel: TransactionFormViewModel, onSaved: () -> Unit, onCancelled: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TransactionFormEffect.Saved -> onSaved()
                TransactionFormEffect.Cancelled -> onCancelled()
                is TransactionFormEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    TransactionFormScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(state: TransactionFormState, snackbarHostState: SnackbarHostState, onEvent: (TransactionFormEvent) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEdit) "Edit transaction" else "New transaction") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(TransactionFormEvent.Cancel) }) {
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
                    onRetry = { onEvent(TransactionFormEvent.Retry) }
                )
                else -> FormBody(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun FormBody(state: TransactionFormState, onEvent: (TransactionFormEvent) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        TypeSelector(
            selected = state.type,
            onSelect = { onEvent(TransactionFormEvent.TypeChanged(it)) },
            enabled = !state.isSubmitting
        )

        PocketPilotTextField(
            value = state.title,
            onValueChange = { onEvent(TransactionFormEvent.TitleChanged(it)) },
            label = "Title",
            errorMessage = state.titleError,
            enabled = !state.isSubmitting
        )

        PocketPilotTextField(
            value = state.amountInput,
            onValueChange = { onEvent(TransactionFormEvent.AmountChanged(it)) },
            label = "Amount (${state.currencyCode})",
            keyboardType = KeyboardType.Decimal,
            errorMessage = state.amountError,
            enabled = !state.isSubmitting
        )

        PocketPilotTextField(
            value = state.category,
            onValueChange = { onEvent(TransactionFormEvent.CategoryChanged(it)) },
            label = "Category",
            placeholder = "e.g. Groceries",
            supportingText = "Optional",
            enabled = !state.isSubmitting
        )

        DateField(
            millis = state.occurredAtMillis,
            onClick = { showDatePicker = true },
            enabled = !state.isSubmitting
        )

        PocketPilotTextField(
            value = state.note,
            onValueChange = { onEvent(TransactionFormEvent.NoteChanged(it)) },
            label = "Note",
            supportingText = "Optional",
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
                onClick = { onEvent(TransactionFormEvent.Cancel) },
                enabled = !state.isSubmitting,
                modifier = Modifier.weight(1f)
            )
            PocketPilotPrimaryButton(
                text = if (state.isEdit) "Save" else "Add",
                onClick = { onEvent(TransactionFormEvent.Submit) },
                enabled = state.canSubmit,
                loading = state.isSubmitting,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            initialMillis = state.occurredAtMillis,
            onConfirm = {
                onEvent(TransactionFormEvent.DateChanged(it))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun TypeSelector(selected: TransactionType, onSelect: (TransactionType) -> Unit, enabled: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)) {
        FilterChip(
            selected = selected == TransactionType.EXPENSE,
            onClick = { onSelect(TransactionType.EXPENSE) },
            label = { Text("Expense") },
            enabled = enabled
        )
        FilterChip(
            selected = selected == TransactionType.INCOME,
            onClick = { onSelect(TransactionType.INCOME) },
            label = { Text("Income") },
            enabled = enabled
        )
    }
}

@Composable
private fun DateField(millis: Long, onClick: () -> Unit, enabled: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)
    ) {
        Text(
            text = "Date",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = DateFormatter.formatFull(millis),
                modifier = Modifier.fillMaxWidth()
            )
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
