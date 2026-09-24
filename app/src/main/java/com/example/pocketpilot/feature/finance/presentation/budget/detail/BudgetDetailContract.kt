package com.example.pocketpilot.feature.finance.presentation.budget.detail

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.finance.domain.model.BudgetProgress

@Immutable
data class BudgetDetailState(
    val progress: UiState<BudgetProgress> = UiState.Loading,
    val isDeleting: Boolean = false,
    val showDeleteConfirmation: Boolean = false
)

sealed interface BudgetDetailEvent : UiEvent {
    data object EditClicked : BudgetDetailEvent
    data object DeleteClicked : BudgetDetailEvent
    data object DeleteConfirmed : BudgetDetailEvent
    data object DeleteDismissed : BudgetDetailEvent
    data object BackClicked : BudgetDetailEvent
    data object Retry : BudgetDetailEvent
}

sealed interface BudgetDetailEffect : UiEffect {
    data class NavigateToEdit(val id: String) : BudgetDetailEffect
    data object NavigateBack : BudgetDetailEffect
    data class ShowError(val message: String) : BudgetDetailEffect
}
