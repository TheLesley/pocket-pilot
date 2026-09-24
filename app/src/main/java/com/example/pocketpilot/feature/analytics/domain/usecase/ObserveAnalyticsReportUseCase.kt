package com.example.pocketpilot.feature.analytics.domain.usecase

import com.example.pocketpilot.feature.analytics.domain.model.AnalyticsReport
import com.example.pocketpilot.feature.analytics.domain.model.DateRange
import com.example.pocketpilot.feature.analytics.domain.model.DateRangeFilter
import com.example.pocketpilot.feature.analytics.domain.model.TrendGranularity
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId

/**
 * Orchestrator use case that observes every persisted transaction and reduces
 * it into a single [AnalyticsReport] for the requested [DateRangeFilter].
 *
 * The full transaction list is streamed rather than a pre-filtered one so
 * different analytics panes (trend vs. current-window totals) can slice the
 * data without spawning multiple concurrent queries.
 */
class ObserveAnalyticsReportUseCase(
    private val transactionRepository: TransactionRepository,
    private val calculateCashFlowSummary: CalculateCashFlowSummaryUseCase = CalculateCashFlowSummaryUseCase(),
    private val calculateCategoryDistribution: CalculateCategoryDistributionUseCase = CalculateCategoryDistributionUseCase(),
    private val calculateTrendSeries: CalculateTrendSeriesUseCase = CalculateTrendSeriesUseCase(),
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val zone: ZoneId = ZoneId.systemDefault()
) {

    operator fun invoke(filter: DateRangeFilter): Flow<AnalyticsReport> = transactionRepository.observeAll().map { transactions ->
        val range = filter.resolve(clock(), zone)
        buildReport(filter, range, transactions)
    }

    private fun buildReport(filter: DateRangeFilter, range: DateRange, transactions: List<Transaction>): AnalyticsReport {
        val currency = transactions.firstOrNull()?.currencyCode ?: DEFAULT_CURRENCY
        val cashFlow = calculateCashFlowSummary(transactions, range, currency)
        val distribution = calculateCategoryDistribution(transactions, range)
        val granularity: TrendGranularity? = when (filter) {
            DateRangeFilter.ThisYear, DateRangeFilter.LastYear -> TrendGranularity.MONTHLY
            else -> null
        }
        val trend = calculateTrendSeries(transactions, range, granularity)
        return AnalyticsReport(
            currencyCode = currency,
            filter = filter,
            range = range,
            cashFlow = cashFlow,
            trend = trend,
            categoryDistribution = distribution
        )
    }

    private companion object {
        const val DEFAULT_CURRENCY = "USD"
    }
}
