package com.example.pocketpilot.feature.finance.presentation.budget.list

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.finance.domain.model.BudgetProgress

@Immutable
data class BudgetListState(val budgets: UiState<List<BudgetProgress>> = UiState.Loading)

sealed interface BudgetListEvent : UiEvent {
    data object AddClicked : BudgetListEvent
    data class BudgetClicked(val id: String) : BudgetListEvent
    data object Retry : BudgetListEvent
}

sealed interface BudgetListEffect : UiEffect {
    data object NavigateToAdd : BudgetListEffect
    data class NavigateToDetail(val id: String) : BudgetListEffect
}
