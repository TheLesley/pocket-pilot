package com.example.pocketpilot.feature.finance.presentation.savings.detail

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.BudgetProgressIndicator
import com.example.pocketpilot.core.designsystem.component.ErrorState
import com.example.pocketpilot.core.designsystem.component.PocketPilotPrimaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotSecondaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotTextField
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoalProgress
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoalStatus
import com.example.pocketpilot.feature.finance.presentation.savings.displayName
import com.example.pocketpilot.feature.finance.presentation.savings.timeRemainingLabel
import com.example.pocketpilot.feature.finance.presentation.util.DateFormatter
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter

@Composable
fun SavingsGoalDetailRoute(viewModel: SavingsGoalDetailViewModel, onNavigateBack: () -> Unit, onNavigateToEdit: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SavingsGoalDetailEffect.NavigateBack -> onNavigateBack()
                is SavingsGoalDetailEffect.NavigateToEdit -> onNavigateToEdit(effect.id)
                is SavingsGoalDetailEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    SavingsGoalDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalDetailScreen(
    state: SavingsGoalDetailState,
    snackbarHostState: SnackbarHostState,
    onEvent: (SavingsGoalDetailEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings goal") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SavingsGoalDetailEvent.BackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val isSuccess = state.progress is UiState.Success
                    IconButton(
                        onClick = { onEvent(SavingsGoalDetailEvent.EditClicked) },
                        enabled = isSuccess && !state.isDeleting
                    ) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                    IconButton(
                        onClick = { onEvent(SavingsGoalDetailEvent.DeleteClicked) },
                        enabled = isSuccess && !state.isDeleting
                    ) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
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
            when (val progress = state.progress) {
                UiState.Idle,
                UiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                UiState.Empty -> ErrorState(
                    title = "Not found",
                    message = "This savings goal no longer exists.",
                    onRetry = null
                )
                is UiState.Error -> ErrorState(
                    message = progress.message,
                    onRetry = { onEvent(SavingsGoalDetailEvent.Retry) }
                )
                is UiState.Success -> DetailBody(
                    progress = progress.data,
                    isMarkingComplete = state.isMarkingComplete,
                    onEvent = onEvent
                )
            }
        }
    }

    if (state.showDeleteConfirmation) {
        DeleteDialog(
            isDeleting = state.isDeleting,
            onConfirm = { onEvent(SavingsGoalDetailEvent.DeleteConfirmed) },
            onDismiss = { onEvent(SavingsGoalDetailEvent.DeleteDismissed) }
        )
    }

    if (state.showContributionDialog) {
        val currencyCode = (state.progress as? UiState.Success<SavingsGoalProgress>)
            ?.data?.goal?.currencyCode ?: "USD"
        ContributionDialog(
            mode = state.contributionMode,
            input = state.contributionInput,
            error = state.contributionError,
            isSubmitting = state.isSubmittingContribution,
            currencyCode = currencyCode,
            onInputChange = { onEvent(SavingsGoalDetailEvent.ContributionInputChanged(it)) },
            onConfirm = { onEvent(SavingsGoalDetailEvent.ContributionSubmitted) },
            onDismiss = { onEvent(SavingsGoalDetailEvent.ContributionDismissed) }
        )
    }
}

@Composable
private fun DetailBody(progress: SavingsGoalProgress, isMarkingComplete: Boolean, onEvent: (SavingsGoalDetailEvent) -> Unit) {
    val goal = progress.goal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        Text(
            text = goal.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        StatusPill(progress = progress)

        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(PocketPilotTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${progress.percent}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = statusColor(progress.status),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                BudgetProgressIndicator(ratio = progress.ratio, height = 14.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Saved " + MoneyFormatter.format(
                            minorUnits = progress.savedMinorUnits,
                            currencyCode = goal.currencyCode
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Remaining " + MoneyFormatter.format(
                            minorUnits = progress.remainingMinorUnits,
                            currencyCode = goal.currencyCode
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor(progress.status)
                    )
                }
                val remainingLabel = progress.timeRemainingLabel()
                if (remainingLabel != null) {
                    Text(
                        text = remainingLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
        ) {
            PocketPilotSecondaryButton(
                text = "Remove",
                onClick = { onEvent(SavingsGoalDetailEvent.RemoveFundsClicked) },
                enabled = !progress.isComplete && progress.savedMinorUnits > 0L,
                modifier = Modifier.weight(1f)
            )
            PocketPilotPrimaryButton(
                text = "Add funds",
                onClick = { onEvent(SavingsGoalDetailEvent.AddFundsClicked) },
                enabled = !progress.isComplete,
                modifier = Modifier.weight(1f)
            )
        }

        PocketPilotSecondaryButton(
            text = if (progress.isComplete) "Completed" else "Mark complete",
            onClick = { onEvent(SavingsGoalDetailEvent.MarkCompleteClicked) },
            enabled = !progress.isComplete && !isMarkingComplete,
            loading = isMarkingComplete,
            modifier = Modifier.fillMaxWidth()
        )

        DetailRow(
            label = "Target",
            value = MoneyFormatter.format(goal.targetMinorUnits, goal.currencyCode)
        )
        DetailRow(
            label = "Target date",
            value = goal.targetDateEpochMillis?.let(DateFormatter::formatFull)
                ?: "No deadline"
        )
        if (!goal.note.isNullOrBlank()) {
            DetailRow(label = "Note", value = goal.note)
        }
        DetailRow(
            label = "Created",
            value = DateFormatter.formatFull(goal.createdAtEpochMillis)
        )
    }
}

@Composable
private fun StatusPill(progress: SavingsGoalProgress) {
    val (container, onContainer) = when (progress.status) {
        SavingsGoalStatus.ON_TRACK ->
            MaterialTheme.colorScheme.primaryContainer to
                MaterialTheme.colorScheme.onPrimaryContainer
        SavingsGoalStatus.AT_RISK ->
            MaterialTheme.colorScheme.tertiaryContainer to
                MaterialTheme.colorScheme.onTertiaryContainer
        SavingsGoalStatus.OVERDUE ->
            MaterialTheme.colorScheme.errorContainer to
                MaterialTheme.colorScheme.onErrorContainer
        SavingsGoalStatus.COMPLETED ->
            MaterialTheme.colorScheme.primaryContainer to
                MaterialTheme.colorScheme.onPrimaryContainer
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = onContainer
    ) {
        Text(
            text = progress.status.displayName(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                horizontal = PocketPilotTheme.spacing.md,
                vertical = PocketPilotTheme.spacing.xs
            )
        )
    }
}

@Composable
private fun statusColor(status: SavingsGoalStatus) = when (status) {
    SavingsGoalStatus.ON_TRACK -> MaterialTheme.colorScheme.primary
    SavingsGoalStatus.AT_RISK -> MaterialTheme.colorScheme.tertiary
    SavingsGoalStatus.OVERDUE -> MaterialTheme.colorScheme.error
    SavingsGoalStatus.COMPLETED -> MaterialTheme.colorScheme.primary
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DeleteDialog(isDeleting: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete savings goal?") },
        text = { Text("This action cannot be undone.") },
        confirmButton = {
            PocketPilotPrimaryButton(
                text = "Delete",
                onClick = onConfirm,
                loading = isDeleting
            )
        },
        dismissButton = {
            PocketPilotSecondaryButton(
                text = "Cancel",
                onClick = onDismiss,
                enabled = !isDeleting
            )
        }
    )
}

@Composable
private fun ContributionDialog(
    mode: ContributionMode,
    input: String,
    error: String?,
    isSubmitting: Boolean,
    currencyCode: String,
    onInputChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when (mode) {
        ContributionMode.Add -> "Add funds"
        ContributionMode.Remove -> "Remove funds"
    }
    val confirmLabel = when (mode) {
        ContributionMode.Add -> "Add"
        ContributionMode.Remove -> "Remove"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            PocketPilotTextField(
                value = input,
                onValueChange = onInputChange,
                label = "Amount ($currencyCode)",
                keyboardType = KeyboardType.Decimal,
                errorMessage = error,
                enabled = !isSubmitting
            )
        },
        confirmButton = {
            PocketPilotPrimaryButton(
                text = confirmLabel,
                onClick = onConfirm,
                loading = isSubmitting,
                enabled = input.isNotBlank()
            )
        },
        dismissButton = {
            PocketPilotSecondaryButton(
                text = "Cancel",
                onClick = onDismiss,
                enabled = !isSubmitting
            )
        }
    )
}
