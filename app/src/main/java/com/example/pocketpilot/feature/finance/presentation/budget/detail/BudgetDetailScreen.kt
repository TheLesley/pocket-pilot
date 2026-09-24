package com.example.pocketpilot.feature.finance.presentation.budget.detail

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.BudgetProgressIndicator
import com.example.pocketpilot.core.designsystem.component.ErrorState
import com.example.pocketpilot.core.designsystem.component.PocketPilotPrimaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotSecondaryButton
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod
import com.example.pocketpilot.feature.finance.domain.model.BudgetProgress
import com.example.pocketpilot.feature.finance.domain.model.BudgetStatus
import com.example.pocketpilot.feature.finance.presentation.util.DateFormatter
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter

@Composable
fun BudgetDetailRoute(viewModel: BudgetDetailViewModel, onNavigateBack: () -> Unit, onNavigateToEdit: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                BudgetDetailEffect.NavigateBack -> onNavigateBack()
                is BudgetDetailEffect.NavigateToEdit -> onNavigateToEdit(effect.id)
                is BudgetDetailEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    BudgetDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(state: BudgetDetailState, snackbarHostState: SnackbarHostState, onEvent: (BudgetDetailEvent) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(BudgetDetailEvent.BackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val isSuccess = state.progress is UiState.Success
                    IconButton(
                        onClick = { onEvent(BudgetDetailEvent.EditClicked) },
                        enabled = isSuccess && !state.isDeleting
                    ) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                    IconButton(
                        onClick = { onEvent(BudgetDetailEvent.DeleteClicked) },
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
                UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                UiState.Empty -> ErrorState(
                    title = "Not found",
                    message = "This budget no longer exists.",
                    onRetry = null
                )
                is UiState.Error -> ErrorState(
                    message = progress.message,
                    onRetry = { onEvent(BudgetDetailEvent.Retry) }
                )
                is UiState.Success -> BudgetDetailBody(progress = progress.data)
            }
        }
    }

    if (state.showDeleteConfirmation) {
        DeleteDialog(
            isDeleting = state.isDeleting,
            onConfirm = { onEvent(BudgetDetailEvent.DeleteConfirmed) },
            onDismiss = { onEvent(BudgetDetailEvent.DeleteDismissed) }
        )
    }
}

@Composable
private fun BudgetDetailBody(progress: BudgetProgress) {
    val budget = progress.budget
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        Text(
            text = budget.name,
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
                        text = "Usage",
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
                        text = "Spent " + MoneyFormatter.format(
                            minorUnits = progress.spentMinorUnits,
                            currencyCode = budget.currencyCode
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val remainingLabel = if (progress.remainingMinorUnits < 0L) {
                        "Over by " + MoneyFormatter.format(
                            minorUnits = -progress.remainingMinorUnits,
                            currencyCode = budget.currencyCode
                        )
                    } else {
                        "Remaining " + MoneyFormatter.format(
                            minorUnits = progress.remainingMinorUnits,
                            currencyCode = budget.currencyCode
                        )
                    }
                    Text(
                        text = remainingLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor(progress.status)
                    )
                }
            }
        }

        DetailRow(
            label = "Limit",
            value = MoneyFormatter.format(budget.limitMinorUnits, budget.currencyCode)
        )
        DetailRow(label = "Period", value = budget.period.label())
        DetailRow(label = "Category", value = budget.categoryId ?: "Any")
        DetailRow(label = "Starts", value = DateFormatter.formatFull(budget.startsAtEpochMillis))
        DetailRow(
            label = "Ends",
            value = budget.endsAtEpochMillis?.let(DateFormatter::formatFull) ?: "No end date"
        )
    }
}

@Composable
private fun StatusPill(progress: BudgetProgress) {
    val (label, container, onContainer) = when (progress.status) {
        BudgetStatus.ON_TRACK -> Triple(
            "On track",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        BudgetStatus.WARNING -> Triple(
            "Nearing limit",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        BudgetStatus.EXCEEDED -> Triple(
            "Exceeded",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = onContainer
    ) {
        Text(
            text = label,
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
private fun statusColor(status: BudgetStatus) = when (status) {
    BudgetStatus.ON_TRACK -> MaterialTheme.colorScheme.primary
    BudgetStatus.WARNING -> MaterialTheme.colorScheme.tertiary
    BudgetStatus.EXCEEDED -> MaterialTheme.colorScheme.error
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

private fun BudgetPeriod.label(): String = when (this) {
    BudgetPeriod.WEEKLY -> "Weekly"
    BudgetPeriod.MONTHLY -> "Monthly"
    BudgetPeriod.YEARLY -> "Yearly"
    BudgetPeriod.CUSTOM -> "Custom"
}

@Composable
private fun DeleteDialog(isDeleting: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete budget?") },
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
