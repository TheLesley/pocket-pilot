package com.example.pocketpilot.feature.finance.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.ErrorState
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.feature.finance.presentation.list.components.ActiveFilterChips
import com.example.pocketpilot.feature.finance.presentation.list.components.TransactionFilterSheet
import com.example.pocketpilot.feature.finance.presentation.list.components.TransactionSearchBar
import com.example.pocketpilot.feature.finance.presentation.util.DateFormatter
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter

@Composable
fun TransactionListRoute(viewModel: TransactionListViewModel, onNavigateToAdd: () -> Unit, onNavigateToDetail: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TransactionListEffect.NavigateToAdd -> onNavigateToAdd()
                is TransactionListEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
            }
        }
    }

    TransactionListScreen(state = state, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(state: TransactionListState, onEvent: (TransactionListEvent) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    IconButton(onClick = { onEvent(TransactionListEvent.OpenFilterSheet) }) {
                        BadgedBox(
                            badge = {
                                if (state.activeFilterCount > 0) {
                                    Badge { Text(state.activeFilterCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Open filters",
                                tint = if (state.hasActiveFilters) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(TransactionListEvent.AddClicked) }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = PocketPilotTheme.spacing.md,
                        vertical = PocketPilotTheme.spacing.sm
                    ),
                verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
            ) {
                TransactionSearchBar(
                    value = state.searchTerm,
                    onValueChange = { onEvent(TransactionListEvent.SearchChanged(it)) },
                    onClear = { onEvent(TransactionListEvent.ClearSearch) }
                )
                ActiveFilterChips(state = state, onEvent = onEvent)
            }
            TransactionListBody(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (state.filterSheetVisible) {
            TransactionFilterSheet(state = state, onEvent = onEvent)
        }
    }
}

@Composable
private fun TransactionListBody(state: TransactionListState, onEvent: (TransactionListEvent) -> Unit, modifier: Modifier = Modifier) {
    when (val transactions = state.transactions) {
        UiState.Idle,
        UiState.Loading -> Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        UiState.Empty -> EmptyPlaceholder(state = state, modifier = modifier)
        is UiState.Success -> TransactionList(
            transactions = transactions.data,
            onClick = { onEvent(TransactionListEvent.TransactionClicked(it.id)) },
            modifier = modifier
        )
        is UiState.Error -> ErrorState(
            message = transactions.message,
            onRetry = { onEvent(TransactionListEvent.Retry) },
            modifier = modifier
        )
    }
}

@Composable
private fun EmptyPlaceholder(state: TransactionListState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(PocketPilotTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (state.hasActiveFilters) "No matches" else "No transactions yet",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = if (state.hasActiveFilters) {
                "Try clearing your filters to see more transactions."
            } else {
                "Tap the + button to record your first transaction."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = PocketPilotTheme.spacing.sm)
        )
    }
}

@Composable
private fun TransactionList(transactions: List<Transaction>, onClick: (Transaction) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = PocketPilotTheme.spacing.md,
            vertical = PocketPilotTheme.spacing.sm
        ),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
    ) {
        items(items = transactions, key = { it.id }) { transaction ->
            TransactionRow(transaction = transaction, onClick = { onClick(transaction) })
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(PocketPilotTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
        ) {
            TypeBadge(type = transaction.type)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = DateFormatter.formatShort(transaction.occurredAtEpochMillis) +
                        (transaction.categoryId?.let { " • $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = signedAmount(transaction),
                style = MaterialTheme.typography.titleMedium,
                color = amountColor(transaction.type),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TypeBadge(type: TransactionType) {
    val (background, label) = when (type) {
        TransactionType.INCOME -> MaterialTheme.colorScheme.tertiaryContainer to "IN"
        TransactionType.EXPENSE -> MaterialTheme.colorScheme.errorContainer to "EX"
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun amountColor(type: TransactionType) = when (type) {
    TransactionType.INCOME -> MaterialTheme.colorScheme.tertiary
    TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
}

private fun signedAmount(transaction: Transaction): String {
    val amount = MoneyFormatter.format(transaction.amountMinorUnits, transaction.currencyCode)
    val prefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
    return "$prefix$amount"
}
