package com.example.pocketpilot.feature.dashboard.presentation

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.dashboard.domain.model.DashboardSummary

@Immutable
data class DashboardState(val summary: UiState<DashboardSummary> = UiState.Loading)

sealed interface DashboardEvent : UiEvent {
    data object Retry : DashboardEvent
    data object AddIncomeClicked : DashboardEvent
    data object AddExpenseClicked : DashboardEvent
    data object TransferClicked : DashboardEvent
    data object ViewAllTransactionsClicked : DashboardEvent
    data class TransactionClicked(val id: String) : DashboardEvent
}

sealed interface DashboardEffect : UiEffect {
    data object NavigateToAddIncome : DashboardEffect
    data object NavigateToAddExpense : DashboardEffect
    data object NavigateToTransfer : DashboardEffect
    data object NavigateToTransactionList : DashboardEffect
    data class NavigateToTransactionDetail(val id: String) : DashboardEffect
    data class ShowMessage(val message: String) : DashboardEffect
}
