package com.example.pocketpilot.feature.analytics.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocketpilot.feature.analytics.domain.usecase.CalculateCashFlowSummaryUseCase
import com.example.pocketpilot.feature.analytics.domain.usecase.CalculateCategoryDistributionUseCase
import com.example.pocketpilot.feature.analytics.domain.usecase.CalculateTrendSeriesUseCase
import com.example.pocketpilot.feature.analytics.domain.usecase.ObserveAnalyticsReportUseCase
import com.example.pocketpilot.feature.analytics.presentation.AnalyticsViewModel
import com.example.pocketpilot.feature.finance.di.FinanceContainer

/**
 * Service locator for the analytics feature. Mirrors the shape of an eventual
 * Hilt module (`pure calculators → orchestrator → view model`) so the swap to
 * `@Module` bindings is mechanical.
 */
object AnalyticsContainer {

    private val calculateCashFlowSummary by lazy { CalculateCashFlowSummaryUseCase() }
    private val calculateCategoryDistribution by lazy { CalculateCategoryDistributionUseCase() }
    private val calculateTrendSeries by lazy { CalculateTrendSeriesUseCase() }

    val observeAnalyticsReport: ObserveAnalyticsReportUseCase by lazy {
        ObserveAnalyticsReportUseCase(
            transactionRepository = FinanceContainer.transactionRepository,
            calculateCashFlowSummary = calculateCashFlowSummary,
            calculateCategoryDistribution = calculateCategoryDistribution,
            calculateTrendSeries = calculateTrendSeries
        )
    }
}

class AnalyticsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return AnalyticsViewModel(AnalyticsContainer.observeAnalyticsReport) as T
    }
}
