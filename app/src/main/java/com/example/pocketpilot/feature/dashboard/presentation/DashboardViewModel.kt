package com.example.pocketpilot.feature.dashboard.presentation

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.dashboard.domain.usecase.ObserveDashboardSummaryUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

class DashboardViewModel(private val observeDashboardSummary: ObserveDashboardSummaryUseCase) :
    BaseViewModel<DashboardState, DashboardEvent, DashboardEffect>(DashboardState()) {

    private var observeJob: Job? = null

    init {
        observe()
    }

    override fun handleEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.Retry -> observe()
            DashboardEvent.AddIncomeClicked -> sendEffect(DashboardEffect.NavigateToAddIncome)
            DashboardEvent.AddExpenseClicked -> sendEffect(DashboardEffect.NavigateToAddExpense)
            DashboardEvent.TransferClicked -> sendEffect(DashboardEffect.NavigateToTransfer)
            DashboardEvent.ViewAllTransactionsClicked -> sendEffect(DashboardEffect.NavigateToTransactionList)
            is DashboardEvent.TransactionClicked -> sendEffect(
                DashboardEffect.NavigateToTransactionDetail(event.id)
            )
        }
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = observeDashboardSummary()
            .onStart { setState { copy(summary = UiState.Loading) } }
            .catch { t ->
                setState {
                    copy(
                        summary = UiState.Error(
                            message = t.message ?: "Failed to load dashboard",
                            throwable = t
                        )
                    )
                }
            }
            .onEach { summary ->
                // Always emit Success — even a zeroed summary is a valid dashboard
                // (quick actions + empty-hint copy). The UI decides how to render
                // an "empty" account using `summary.hasAnyActivity`.
                setState { copy(summary = UiState.Success(summary)) }
            }
            .launchIn(viewModelScope)
    }
}
