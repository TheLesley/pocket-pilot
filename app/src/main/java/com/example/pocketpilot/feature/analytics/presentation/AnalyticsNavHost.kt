package com.example.pocketpilot.feature.analytics.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketpilot.feature.analytics.di.AnalyticsViewModelFactory
import com.example.pocketpilot.feature.analytics.presentation.breakdown.CategoryBreakdownRoute
import com.example.pocketpilot.feature.analytics.presentation.overview.AnalyticsOverviewRoute
import com.example.pocketpilot.feature.analytics.presentation.trends.SpendingTrendsRoute

/**
 * Feature-local navigation for analytics. The overview, breakdown, and trends
 * screens all share a single [AnalyticsViewModel] so switching between them
 * doesn't restart the observation flow or reset the user's filter selection.
 */
sealed interface AnalyticsDestination {
    data object Overview : AnalyticsDestination
    data object CategoryBreakdown : AnalyticsDestination
    data object SpendingTrends : AnalyticsDestination
}

@Composable
fun AnalyticsNavHost(onExit: () -> Unit) {
    var destination: AnalyticsDestination by rememberSaveable(stateSaver = AnalyticsDestinationSaver) {
        mutableStateOf(AnalyticsDestination.Overview)
    }
    val viewModel: AnalyticsViewModel = viewModel(
        key = "analytics.root",
        factory = remember { AnalyticsViewModelFactory() }
    )

    when (destination) {
        AnalyticsDestination.Overview -> AnalyticsOverviewRoute(
            viewModel = viewModel,
            onNavigateToCategoryBreakdown = { destination = AnalyticsDestination.CategoryBreakdown },
            onNavigateToSpendingTrends = { destination = AnalyticsDestination.SpendingTrends },
            onNavigateBack = onExit
        )
        AnalyticsDestination.CategoryBreakdown -> CategoryBreakdownRoute(
            viewModel = viewModel,
            onNavigateBack = { destination = AnalyticsDestination.Overview }
        )
        AnalyticsDestination.SpendingTrends -> SpendingTrendsRoute(
            viewModel = viewModel,
            onNavigateBack = { destination = AnalyticsDestination.Overview }
        )
    }
}

private val AnalyticsDestinationSaver = Saver<AnalyticsDestination, String>(
    save = { dest ->
        when (dest) {
            AnalyticsDestination.Overview -> "overview"
            AnalyticsDestination.CategoryBreakdown -> "breakdown"
            AnalyticsDestination.SpendingTrends -> "trends"
        }
    },
    restore = { raw ->
        when (raw) {
            "overview" -> AnalyticsDestination.Overview
            "breakdown" -> AnalyticsDestination.CategoryBreakdown
            "trends" -> AnalyticsDestination.SpendingTrends
            else -> AnalyticsDestination.Overview
        }
    }
)
