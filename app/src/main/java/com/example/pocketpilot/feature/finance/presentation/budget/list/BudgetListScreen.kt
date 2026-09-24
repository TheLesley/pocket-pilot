package com.example.pocketpilot.feature.finance.presentation.budget.list

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.BudgetProgressIndicator
import com.example.pocketpilot.core.designsystem.component.ErrorState
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.finance.domain.model.BudgetProgress
import com.example.pocketpilot.feature.finance.domain.model.BudgetStatus
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter

@Composable
fun BudgetListRoute(viewModel: BudgetListViewModel, onNavigateToAdd: () -> Unit, onNavigateToDetail: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                BudgetListEffect.NavigateToAdd -> onNavigateToAdd()
                is BudgetListEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
            }
        }
    }

    BudgetListScreen(state = state, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreen(state: BudgetListState, onEvent: (BudgetListEvent) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Budgets") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(BudgetListEvent.AddClicked) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add budget")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val budgets = state.budgets) {
                UiState.Idle,
                UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                UiState.Empty -> EmptyPlaceholder()
                is UiState.Error -> ErrorState(
                    message = budgets.message,
                    onRetry = { onEvent(BudgetListEvent.Retry) }
                )
                is UiState.Success -> BudgetList(
                    budgets = budgets.data,
                    onClick = { onEvent(BudgetListEvent.BudgetClicked(it.budget.id)) }
                )
            }
        }
    }
}

@Composable
private fun EmptyPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PocketPilotTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No budgets yet",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Tap the + button to set your first spending limit.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = PocketPilotTheme.spacing.sm)
        )
    }
}

@Composable
private fun BudgetList(budgets: List<BudgetProgress>, onClick: (BudgetProgress) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = PocketPilotTheme.spacing.md,
            vertical = PocketPilotTheme.spacing.sm
        ),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
    ) {
        items(items = budgets, key = { it.budget.id }) { progress ->
            BudgetRow(progress = progress, onClick = { onClick(progress) })
        }
    }
}

@Composable
internal fun BudgetRow(progress: BudgetProgress, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
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
                Column(modifier = Modifier.padding(end = PocketPilotTheme.spacing.sm)) {
                    Text(
                        text = progress.budget.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = progress.budget.period.displayName() +
                            (progress.budget.categoryId?.let { " • $it" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${progress.percent}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor(progress.status),
                    fontWeight = FontWeight.SemiBold
                )
            }
            BudgetProgressIndicator(ratio = progress.ratio)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent " + MoneyFormatter.format(
                        minorUnits = progress.spentMinorUnits,
                        currencyCode = progress.budget.currencyCode
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Limit " + MoneyFormatter.format(
                        minorUnits = progress.limitMinorUnits,
                        currencyCode = progress.budget.currencyCode
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun statusColor(status: BudgetStatus) = when (status) {
    BudgetStatus.ON_TRACK -> MaterialTheme.colorScheme.primary
    BudgetStatus.WARNING -> MaterialTheme.colorScheme.tertiary
    BudgetStatus.EXCEEDED -> MaterialTheme.colorScheme.error
}

internal fun com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod.displayName(): String = when (this) {
    com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod.WEEKLY -> "Weekly"
    com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod.MONTHLY -> "Monthly"
    com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod.YEARLY -> "Yearly"
    com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod.CUSTOM -> "Custom"
}
