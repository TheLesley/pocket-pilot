package com.example.pocketpilot.feature.finance.presentation.savings.list

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoalProgress

@Immutable
data class SavingsGoalListState(val goals: UiState<List<SavingsGoalProgress>> = UiState.Loading)

sealed interface SavingsGoalListEvent : UiEvent {
    data object AddClicked : SavingsGoalListEvent
    data class GoalClicked(val id: String) : SavingsGoalListEvent
    data object Retry : SavingsGoalListEvent
}

sealed interface SavingsGoalListEffect : UiEffect {
    data object NavigateToAdd : SavingsGoalListEffect
    data class NavigateToDetail(val id: String) : SavingsGoalListEffect
}
