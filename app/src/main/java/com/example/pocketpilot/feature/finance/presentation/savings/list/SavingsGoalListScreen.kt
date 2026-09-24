package com.example.pocketpilot.feature.finance.presentation.savings.list

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
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoalProgress
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoalStatus
import com.example.pocketpilot.feature.finance.presentation.savings.timeRemainingLabel
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter

@Composable
fun SavingsGoalListRoute(viewModel: SavingsGoalListViewModel, onNavigateToAdd: () -> Unit, onNavigateToDetail: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SavingsGoalListEffect.NavigateToAdd -> onNavigateToAdd()
                is SavingsGoalListEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
            }
        }
    }

    SavingsGoalListScreen(state = state, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalListScreen(state: SavingsGoalListState, onEvent: (SavingsGoalListEvent) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Savings goals") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(SavingsGoalListEvent.AddClicked) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add savings goal")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val goals = state.goals) {
                UiState.Idle,
                UiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                UiState.Empty -> EmptyPlaceholder()
                is UiState.Error -> ErrorState(
                    message = goals.message,
                    onRetry = { onEvent(SavingsGoalListEvent.Retry) }
                )
                is UiState.Success -> GoalList(
                    goals = goals.data,
                    onClick = { onEvent(SavingsGoalListEvent.GoalClicked(it.goal.id)) }
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
            text = "No savings goals yet",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Tap the + button to start saving toward your first target.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = PocketPilotTheme.spacing.sm)
        )
    }
}

@Composable
private fun GoalList(goals: List<SavingsGoalProgress>, onClick: (SavingsGoalProgress) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = PocketPilotTheme.spacing.md,
            vertical = PocketPilotTheme.spacing.sm
        ),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
    ) {
        items(items = goals, key = { it.goal.id }) { progress ->
            SavingsGoalRow(progress = progress, onClick = { onClick(progress) })
        }
    }
}

@Composable
internal fun SavingsGoalRow(progress: SavingsGoalProgress, onClick: () -> Unit) {
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
                        text = progress.goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    val subtitle = progress.timeRemainingLabel() ?: "No deadline"
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${progress.percent}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = savingsStatusColor(progress.status),
                    fontWeight = FontWeight.SemiBold
                )
            }
            BudgetProgressIndicator(ratio = progress.ratio)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Saved " + MoneyFormatter.format(
                        minorUnits = progress.savedMinorUnits,
                        currencyCode = progress.goal.currencyCode
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Target " + MoneyFormatter.format(
                        minorUnits = progress.targetMinorUnits,
                        currencyCode = progress.goal.currencyCode
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun savingsStatusColor(status: SavingsGoalStatus) = when (status) {
    SavingsGoalStatus.ON_TRACK -> MaterialTheme.colorScheme.primary
    SavingsGoalStatus.AT_RISK -> MaterialTheme.colorScheme.tertiary
    SavingsGoalStatus.OVERDUE -> MaterialTheme.colorScheme.error
    SavingsGoalStatus.COMPLETED -> MaterialTheme.colorScheme.primary
}
