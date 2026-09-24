package com.example.pocketpilot.feature.analytics.presentation.trends

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.ErrorState
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.analytics.domain.model.AnalyticsReport
import com.example.pocketpilot.feature.analytics.domain.model.TrendGranularity
import com.example.pocketpilot.feature.analytics.domain.model.TrendSeries
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsEffect
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsEvent
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsState
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsViewModel
import com.example.pocketpilot.feature.analytics.presentation.components.BarGroup
import com.example.pocketpilot.feature.analytics.presentation.components.CustomRangeDialog
import com.example.pocketpilot.feature.analytics.presentation.components.DateRangeChips
import com.example.pocketpilot.feature.analytics.presentation.components.GroupedBarChart
import com.example.pocketpilot.feature.analytics.presentation.components.LineSeries
import com.example.pocketpilot.feature.analytics.presentation.components.TrendLineChart
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter

@Composable
fun SpendingTrendsRoute(viewModel: AnalyticsViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AnalyticsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
    SpendingTrendsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingTrendsScreen(
    state: AnalyticsState,
    onEvent: (AnalyticsEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending trends") },
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
                    UiState.Empty -> EmptyTrends()
                    is UiState.Success -> TrendsContent(report = report.data)
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
private fun EmptyTrends() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No trend yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Trends appear once you have transactions in the selected window.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = PocketPilotTheme.spacing.sm)
        )
    }
}

@Composable
private fun TrendsContent(report: AnalyticsReport) {
    val trend = report.trend
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        LineChartCard(trend = trend, currencyCode = report.currencyCode)
        BarChartCard(trend = trend, currencyCode = report.currencyCode)
        NetRunwayCard(trend = trend, currencyCode = report.currencyCode)
        Spacer(Modifier.height(PocketPilotTheme.spacing.md))
    }
}

@Composable
private fun LineChartCard(trend: TrendSeries, currencyCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(PocketPilotTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
        ) {
            Text(
                text = "${trend.granularity.title()} trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            TrendLineChart(
                labels = trend.points.map { it.label },
                series = listOf(
                    LineSeries(
                        label = "Income",
                        values = trend.points.map { it.incomeMinorUnits.toFloat() },
                        color = PocketPilotTheme.extendedColors.income
                    ),
                    LineSeries(
                        label = "Expenses",
                        values = trend.points.map { it.expenseMinorUnits.toFloat() },
                        color = PocketPilotTheme.extendedColors.expense
                    )
                )
            )
            LegendRow()
            Text(
                text = "Values in $currencyCode.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BarChartCard(trend: TrendSeries, currencyCode: String) {
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
                text = "Compare income vs. expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            val incomeColor = PocketPilotTheme.extendedColors.income
            val expenseColor = PocketPilotTheme.extendedColors.expense
            GroupedBarChart(
                groups = trend.points.map { point ->
                    BarGroup(
                        label = point.label,
                        bars = listOf(
                            BarGroup.BarValue(point.incomeMinorUnits.toFloat(), incomeColor),
                            BarGroup.BarValue(point.expenseMinorUnits.toFloat(), expenseColor)
                        )
                    )
                }
            )
            LegendRow()
            val peakExpense = trend.points.maxByOrNull { it.expenseMinorUnits }
            if (peakExpense != null && peakExpense.expenseMinorUnits > 0L) {
                Text(
                    text = "Highest spend: ${peakExpense.label} · " +
                        MoneyFormatter.format(peakExpense.expenseMinorUnits, currencyCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NetRunwayCard(trend: TrendSeries, currencyCode: String) {
    val net = trend.points.map { it.netMinorUnits.toFloat() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(PocketPilotTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
        ) {
            Text(
                text = "Net flow per period",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            TrendLineChart(
                labels = trend.points.map { it.label },
                series = listOf(
                    LineSeries(
                        label = "Net",
                        values = net,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            )
            val cumulative = trend.points.fold(0L) { acc, p -> acc + p.netMinorUnits }
            Text(
                text = "Cumulative net: ${MoneyFormatter.format(cumulative, currencyCode)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        LegendDot(color = PocketPilotTheme.extendedColors.income, label = "Income")
        LegendDot(color = PocketPilotTheme.extendedColors.expense, label = "Expenses")
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.padding(2.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

private fun TrendGranularity.title(): String = when (this) {
    TrendGranularity.MONTHLY -> "Monthly"
    TrendGranularity.YEARLY -> "Yearly"
}
