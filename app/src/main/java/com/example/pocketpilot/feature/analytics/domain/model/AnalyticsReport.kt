package com.example.pocketpilot.feature.analytics.domain.model

import androidx.compose.runtime.Immutable

/**
 * Composite object emitted by `ObserveAnalyticsReportUseCase` — every screen in
 * the analytics feature renders slices of this report so the ViewModel only
 * needs to observe a single flow.
 */
@Immutable
data class AnalyticsReport(
    val currencyCode: String,
    val filter: DateRangeFilter,
    val range: DateRange,
    val cashFlow: CashFlowSummary,
    val trend: TrendSeries,
    val categoryDistribution: CategoryDistribution
) {
    val isEmpty: Boolean get() = cashFlow.isEmpty && categoryDistribution.isEmpty && trend.isEmpty
}
