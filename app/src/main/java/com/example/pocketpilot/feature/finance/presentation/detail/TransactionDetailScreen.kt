package com.example.pocketpilot.feature.finance.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.pocketpilot.core.designsystem.component.PocketPilotPrimaryButton
import com.example.pocketpilot.core.designsystem.component.PocketPilotSecondaryButton
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.feature.finance.presentation.util.DateFormatter
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter

@Composable
fun TransactionDetailRoute(viewModel: TransactionDetailViewModel, onNavigateBack: () -> Unit, onNavigateToEdit: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TransactionDetailEffect.NavigateBack -> onNavigateBack()
                is TransactionDetailEffect.NavigateToEdit -> onNavigateToEdit(effect.id)
                is TransactionDetailEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    TransactionDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    state: TransactionDetailState,
    snackbarHostState: SnackbarHostState,
    onEvent: (TransactionDetailEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(TransactionDetailEvent.BackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val isSuccess = state.transaction is UiState.Success
                    IconButton(
                        onClick = { onEvent(TransactionDetailEvent.EditClicked) },
                        enabled = isSuccess && !state.isDeleting
                    ) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                    IconButton(
                        onClick = { onEvent(TransactionDetailEvent.DeleteClicked) },
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
            when (val tx = state.transaction) {
                UiState.Idle,
                UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                UiState.Empty -> ErrorState(
                    title = "Not found",
                    message = "This transaction no longer exists.",
                    onRetry = null
                )
                is UiState.Error -> ErrorState(
                    message = tx.message,
                    onRetry = { onEvent(TransactionDetailEvent.Retry) }
                )
                is UiState.Success -> TransactionDetailBody(transaction = tx.data)
            }
        }
    }

    if (state.showDeleteConfirmation) {
        DeleteDialog(
            isDeleting = state.isDeleting,
            onConfirm = { onEvent(TransactionDetailEvent.DeleteConfirmed) },
            onDismiss = { onEvent(TransactionDetailEvent.DeleteDismissed) }
        )
    }
}

@Composable
private fun TransactionDetailBody(transaction: Transaction) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        Text(
            text = transaction.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        val amountLabel = MoneyFormatter.format(transaction.amountMinorUnits, transaction.currencyCode)
        val prefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
        Text(
            text = "$prefix$amountLabel",
            style = MaterialTheme.typography.displaySmall,
            color = if (transaction.type == TransactionType.INCOME) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
        DetailRow(label = "Type", value = transaction.type.name.lowercase().replaceFirstChar { it.uppercase() })
        DetailRow(label = "Date", value = DateFormatter.formatFull(transaction.occurredAtEpochMillis))
        DetailRow(label = "Category", value = transaction.categoryId ?: "Uncategorised")
        if (!transaction.note.isNullOrBlank()) {
            Spacer(Modifier.height(PocketPilotTheme.spacing.sm))
            Text(text = "Note", style = MaterialTheme.typography.labelMedium)
            Text(text = transaction.note, style = MaterialTheme.typography.bodyMedium)
        }
    }
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
        title = { Text("Delete transaction?") },
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

@Suppress("unused")
@Composable
private fun BackTextButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text("Back") }
}
