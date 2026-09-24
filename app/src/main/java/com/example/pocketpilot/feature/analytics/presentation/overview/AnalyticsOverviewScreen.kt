package com.example.pocketpilot.feature.analytics.presentation.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketpilot.core.designsystem.component.ErrorState
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.analytics.domain.model.AnalyticsReport
import com.example.pocketpilot.feature.analytics.domain.model.CashFlowSummary
import com.example.pocketpilot.feature.analytics.domain.model.CategoryDistribution
import com.example.pocketpilot.feature.analytics.domain.model.DateRangeFilter
import com.example.pocketpilot.feature.analytics.domain.model.TrendSeries
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsEffect
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsEvent
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsState
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsViewModel
import com.example.pocketpilot.feature.analytics.presentation.components.BarGroup
import com.example.pocketpilot.feature.analytics.presentation.components.CustomRangeDialog
import com.example.pocketpilot.feature.analytics.presentation.components.DateRangeChips
import com.example.pocketpilot.feature.analytics.presentation.components.DonutChart
import com.example.pocketpilot.feature.analytics.presentation.components.DonutLegendItem
import com.example.pocketpilot.feature.analytics.presentation.components.DonutSlice
import com.example.pocketpilot.feature.analytics.presentation.components.GroupedBarChart
import com.example.pocketpilot.feature.analytics.presentation.util.ChartPalette
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter

@Composable
fun AnalyticsOverviewRoute(
    viewModel: AnalyticsViewModel,
    onNavigateToCategoryBreakdown: () -> Unit,
    onNavigateToSpendingTrends: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AnalyticsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    AnalyticsOverviewScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onNavigateToCategoryBreakdown = onNavigateToCategoryBreakdown,
        onNavigateToSpendingTrends = onNavigateToSpendingTrends,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsOverviewScreen(
    state: AnalyticsState,
    onEvent: (AnalyticsEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateToCategoryBreakdown: () -> Unit = {},
    onNavigateToSpendingTrends: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Analytics") }) },
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
                    UiState.Empty -> EmptyState(filter = state.filter)
                    is UiState.Success -> AnalyticsOverviewContent(
                        report = report.data,
                        onNavigateToCategoryBreakdown = onNavigateToCategoryBreakdown,
                        onNavigateToSpendingTrends = onNavigateToSpendingTrends
                    )
                    is UiState.Error -> ErrorState(
                        message = report.message,
                        onRetry = { onEvent(AnalyticsEvent.Retry) }
                    )
                }
            }
        }

        if (state.customPickerVisible) {
            val currentCustom = state.filter as? DateRangeFilter.Custom
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

    // Back handling kept as an intentional no-op parameter so the caller can
    // wire it into the surrounding NavHost.
    onNavigateBack.let { }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(filter: DateRangeFilter) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PocketPilotTheme.spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nothing to report for ${filter.label().lowercase()}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Add transactions or pick a different range to see your numbers.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = PocketPilotTheme.spacing.sm)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalyticsOverviewContent(
    report: AnalyticsReport,
    onNavigateToCategoryBreakdown: () -> Unit,
    onNavigateToSpendingTrends: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
    ) {
        CashFlowHeadlineCard(cashFlow = report.cashFlow)
        IncomeExpenseCard(cashFlow = report.cashFlow)
        SavingsRateCard(cashFlow = report.cashFlow)
        CategoryPreviewCard(
            distribution = report.categoryDistribution,
            currencyCode = report.currencyCode,
            onSeeAll = onNavigateToCategoryBreakdown
        )
        TrendPreviewCard(
            trend = report.trend,
            currencyCode = report.currencyCode,
            onSeeAll = onNavigateToSpendingTrends
        )
        Spacer(Modifier.height(PocketPilotTheme.spacing.md))
    }
}

@Composable
private fun CashFlowHeadlineCard(cashFlow: CashFlowSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(PocketPilotTheme.spacing.md)) {
            Text(
                text = "Net cash flow",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = MoneyFormatter.format(cashFlow.netMinorUnits, cashFlow.currencyCode),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = PocketPilotTheme.spacing.xs)
            )
            Text(
                text = "${cashFlow.transactionCount} transactions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = PocketPilotTheme.spacing.xxs)
            )
        }
    }
}

@Composable
private fun IncomeExpenseCard(cashFlow: CashFlowSummary) {
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
                text = "Income vs. expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            val incomeColor = PocketPilotTheme.extendedColors.income
            val expenseColor = PocketPilotTheme.extendedColors.expense
            GroupedBarChart(
                groups = listOf(
                    BarGroup(
                        label = "Income",
                        bars = listOf(BarGroup.BarValue(cashFlow.incomeMinorUnits.toFloat(), incomeColor))
                    ),
                    BarGroup(
                        label = "Expenses",
                        bars = listOf(BarGroup.BarValue(cashFlow.expenseMinorUnits.toFloat(), expenseColor))
                    )
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CurrencyStat(
                    label = "Income",
                    amount = MoneyFormatter.format(cashFlow.incomeMinorUnits, cashFlow.currencyCode),
                    tint = incomeColor
                )
                CurrencyStat(
                    label = "Expenses",
                    amount = MoneyFormatter.format(cashFlow.expenseMinorUnits, cashFlow.currencyCode),
                    tint = expenseColor
                )
            }
        }
    }
}

@Composable
private fun CurrencyStat(label: String, amount: String, tint: androidx.compose.ui.graphics.Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tint)
            )
            Spacer(Modifier.padding(2.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SavingsRateCard(cashFlow: CashFlowSummary) {
    val rate = cashFlow.savingsRate.coerceIn(-1f, 1f)
    val displayFraction = if (rate >= 0f) rate else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(PocketPilotTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)
        ) {
            Text(
                text = "Savings rate",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = String.format("%.0f%%", rate * 100f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (rate >= 0f) {
                    PocketPilotTheme.extendedColors.income
                } else {
                    PocketPilotTheme.extendedColors.expense
                }
            )
            LinearProgressIndicator(
                progress = { displayFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Text(
                text = if (rate < 0f) {
                    "Spending exceeds income this window."
                } else {
                    "Portion of income kept after expenses."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPreviewCard(distribution: CategoryDistribution, currencyCode: String, onSeeAll: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
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
                    text = "Category breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onSeeAll) { Text("See all") }
            }
            if (distribution.isEmpty) {
                Text(
                    text = "No expenses recorded in this range.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val palette = ChartPalette.categorical()
                val topShares = distribution.shares.take(6)
                val slices = topShares.mapIndexed { index, share ->
                    DonutSlice(
                        label = share.label,
                        value = share.amountMinorUnits.toFloat(),
                        color = ChartPalette.colorFor(index, palette),
                        amountText = MoneyFormatter.format(share.amountMinorUnits, currencyCode)
                    )
                }
                DonutChart(
                    slices = slices,
                    centerLabel = "Total spent",
                    centerValue = MoneyFormatter.format(distribution.totalExpenseMinorUnits, currencyCode)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.xs)
                ) {
                    slices.forEach { slice ->
                        DonutLegendItem(color = slice.color, label = slice.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendPreviewCard(trend: TrendSeries, currencyCode: String, onSeeAll: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
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
                    text = "Spending trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onSeeAll) { Text("Explore") }
            }
            if (trend.isEmpty) {
                Text(
                    text = "Not enough data to draw a trend yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val previewCount = trend.points.size.coerceAtMost(6)
                val labels = trend.points.takeLast(previewCount).map { it.label }
                val expenses = trend.points.takeLast(previewCount).map { it.expenseMinorUnits.toFloat() }
                val income = trend.points.takeLast(previewCount).map { it.incomeMinorUnits.toFloat() }
                com.example.pocketpilot.feature.analytics.presentation.components.TrendLineChart(
                    labels = labels,
                    series = listOf(
                        com.example.pocketpilot.feature.analytics.presentation.components.LineSeries(
                            label = "Expenses",
                            values = expenses,
                            color = PocketPilotTheme.extendedColors.expense
                        ),
                        com.example.pocketpilot.feature.analytics.presentation.components.LineSeries(
                            label = "Income",
                            values = income,
                            color = PocketPilotTheme.extendedColors.income
                        )
                    )
                )
                Text(
                    text = "Latest ${labels.size} periods, in $currencyCode.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
