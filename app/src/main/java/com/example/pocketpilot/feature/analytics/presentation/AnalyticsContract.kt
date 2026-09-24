package com.example.pocketpilot.feature.analytics.presentation

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.analytics.domain.model.AnalyticsReport
import com.example.pocketpilot.feature.analytics.domain.model.DateRangeFilter
import java.time.LocalDate

@Immutable
data class AnalyticsState(
    val filter: DateRangeFilter = DateRangeFilter.ThisMonth,
    val availablePresets: List<DateRangeFilter> = DateRangeFilter.Presets,
    val customPickerVisible: Boolean = false,
    val report: UiState<AnalyticsReport> = UiState.Loading
)

sealed interface AnalyticsEvent : UiEvent {
    data object Retry : AnalyticsEvent
    data class FilterSelected(val filter: DateRangeFilter) : AnalyticsEvent
    data object CustomRangeRequested : AnalyticsEvent
    data object CustomRangeDismissed : AnalyticsEvent
    data class CustomRangeConfirmed(val startInclusive: LocalDate, val endInclusive: LocalDate) : AnalyticsEvent
}

sealed interface AnalyticsEffect : UiEffect {
    data class ShowMessage(val message: String) : AnalyticsEffect
}
