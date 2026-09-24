package com.example.pocketpilot.feature.analytics.presentation.breakdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.ErrorState
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.analytics.domain.model.AnalyticsReport
import com.example.pocketpilot.feature.analytics.domain.model.CategoryDistribution
import com.example.pocketpilot.feature.analytics.domain.model.CategoryShare
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsEffect
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsEvent
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsState
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsViewModel
import com.example.pocketpilot.feature.analytics.presentation.components.CustomRangeDialog
import com.example.pocketpilot.feature.analytics.presentation.components.DateRangeChips
import com.example.pocketpilot.feature.analytics.presentation.components.DonutChart
import com.example.pocketpilot.feature.analytics.presentation.components.DonutSlice
import com.example.pocketpilot.feature.analytics.presentation.util.ChartPalette
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter

@Composable
fun CategoryBreakdownRoute(viewModel: AnalyticsViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AnalyticsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
    CategoryBreakdownScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBreakdownScreen(
    state: AnalyticsState,
    onEvent: (AnalyticsEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category breakdown") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = PocketPilotTheme.spacing.md)
                .padding(top = PocketPilotTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
        ) {
            DateRangeChips(
                selected = state.filter,
                presets = state.availablePresets,
                onPresetSelected = { onEvent(AnalyticsEvent.FilterSelected(it)) },
                onCustomRequested = { onEvent(AnalyticsEvent.CustomRangeRequested) }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (val report = state.report) {
                    UiState.Idle,
                    UiState.Loading -> LoadingBox()
                    UiState.Empty -> EmptyBreakdown()
                    is UiState.Success -> BreakdownContent(report = report.data)
                    is UiState.Error -> ErrorState(
                        message = report.message,
                        onRetry = { onEvent(AnalyticsEvent.Retry) }
                    )
                }
            }
        }

        if (state.customPickerVisible) {
            val currentCustom = state.filter as?
                com.example.pocketpilot.feature.analytics.domain.model.DateRangeFilter.Custom
            CustomRangeDialog(
                initialStart = currentCustom?.startInclusive,
                initialEnd = currentCustom?.endInclusive,
                onDismiss = { onEvent(AnalyticsEvent.CustomRangeDismissed) },
                onConfirm = { start, end ->
                    onEvent(AnalyticsEvent.CustomRangeConfirmed(start, end))
                }
            )
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyBreakdown() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No spending to slice yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Log a few expenses in this range to see how they distribute.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = PocketPilotTheme.spacing.sm)
        )
    }
}

@Composable
private fun BreakdownContent(report: AnalyticsReport) {
    val distribution = report.categoryDistribution
    val palette = ChartPalette.categorical()
    val slicesWithColor = remember(distribution, palette) {
        distribution.shares.mapIndexed { index, share ->
            share to ChartPalette.colorFor(index, palette)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        DonutCard(
            distribution = distribution,
            currencyCode = report.currencyCode,
            slicesWithColor = slicesWithColor
        )
        CategoryList(
            slicesWithColor = slicesWithColor,
            currencyCode = report.currencyCode
        )
        Spacer(Modifier.height(PocketPilotTheme.spacing.md))
    }
}

@Composable
private fun DonutCard(distribution: CategoryDistribution, currencyCode: String, slicesWithColor: List<Pair<CategoryShare, Color>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(PocketPilotTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
        ) {
            Text(
                text = "Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            DonutChart(
                slices = slicesWithColor.map { (share, color) ->
                    DonutSlice(
                        label = share.label,
                        value = share.amountMinorUnits.toFloat(),
                        color = color,
                        amountText = MoneyFormatter.format(share.amountMinorUnits, currencyCode)
                    )
                },
                centerLabel = "Total spent",
                centerValue = MoneyFormatter.format(distribution.totalExpenseMinorUnits, currencyCode)
            )
        }
    }
}

@Composable
private fun CategoryList(slicesWithColor: List<Pair<CategoryShare, Color>>, currencyCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(PocketPilotTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
        ) {
            Text(
                text = "By category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            slicesWithColor.forEach { (share, color) ->
                CategoryRow(share = share, color = color, currencyCode = currencyCode)
            }
        }
    }
}

@Composable
private fun CategoryRow(share: CategoryShare, color: Color, currencyCode: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xxs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.padding(2.dp))
                Text(
                    text = share.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "${String.format("%.0f%%", share.share * 100f)} · " +
                    MoneyFormatter.format(share.amountMinorUnits, currencyCode),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { share.share.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color
        )
        Text(
            text = "${share.transactionCount} transactions",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
