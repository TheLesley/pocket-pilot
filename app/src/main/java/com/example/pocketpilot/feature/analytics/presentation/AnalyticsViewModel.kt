package com.example.pocketpilot.feature.analytics.presentation

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.analytics.domain.model.DateRangeFilter
import com.example.pocketpilot.feature.analytics.domain.usecase.ObserveAnalyticsReportUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

class AnalyticsViewModel(
    private val observeAnalyticsReport: ObserveAnalyticsReportUseCase,
    initialFilter: DateRangeFilter = DateRangeFilter.ThisMonth
) : BaseViewModel<AnalyticsState, AnalyticsEvent, AnalyticsEffect>(
    AnalyticsState(filter = initialFilter)
) {

    private var observeJob: Job? = null

    init {
        observe(initialFilter)
    }

    override fun handleEvent(event: AnalyticsEvent) {
        when (event) {
            AnalyticsEvent.Retry -> observe(currentState.filter)
            is AnalyticsEvent.FilterSelected -> {
                if (event.filter == currentState.filter && currentState.report is UiState.Success) return
                setState { copy(filter = event.filter) }
                observe(event.filter)
            }
            AnalyticsEvent.CustomRangeRequested -> setState { copy(customPickerVisible = true) }
            AnalyticsEvent.CustomRangeDismissed -> setState { copy(customPickerVisible = false) }
            is AnalyticsEvent.CustomRangeConfirmed -> {
                if (event.endInclusive.isBefore(event.startInclusive)) {
                    sendEffect(AnalyticsEffect.ShowMessage("End date must be on or after the start date."))
                    return
                }
                val filter = DateRangeFilter.Custom(
                    startInclusive = event.startInclusive,
                    endInclusive = event.endInclusive
                )
                setState { copy(filter = filter, customPickerVisible = false) }
                observe(filter)
            }
        }
    }

    private fun observe(filter: DateRangeFilter) {
        observeJob?.cancel()
        observeJob = observeAnalyticsReport(filter)
            .onStart { setState { copy(report = UiState.Loading) } }
            .catch { t ->
                setState {
                    copy(
                        report = UiState.Error(
                            message = t.message ?: "Failed to load analytics",
                            throwable = t
                        )
                    )
                }
            }
            .onEach { report ->
                setState {
                    copy(
                        report = if (report.isEmpty) UiState.Empty else UiState.Success(report)
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
