package com.example.pocketpilot.feature.finance.presentation.savings.detail

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoalProgress

enum class ContributionMode { Add, Remove }

@Immutable
data class SavingsGoalDetailState(
    val progress: UiState<SavingsGoalProgress> = UiState.Loading,
    val isDeleting: Boolean = false,
    val isMarkingComplete: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showContributionDialog: Boolean = false,
    val contributionMode: ContributionMode = ContributionMode.Add,
    val contributionInput: String = "",
    val contributionError: String? = null,
    val isSubmittingContribution: Boolean = false
)

sealed interface SavingsGoalDetailEvent : UiEvent {
    data object EditClicked : SavingsGoalDetailEvent
    data object DeleteClicked : SavingsGoalDetailEvent
    data object DeleteConfirmed : SavingsGoalDetailEvent
    data object DeleteDismissed : SavingsGoalDetailEvent
    data object BackClicked : SavingsGoalDetailEvent
    data object Retry : SavingsGoalDetailEvent
    data object AddFundsClicked : SavingsGoalDetailEvent
    data object RemoveFundsClicked : SavingsGoalDetailEvent
    data class ContributionInputChanged(val value: String) : SavingsGoalDetailEvent
    data object ContributionSubmitted : SavingsGoalDetailEvent
    data object ContributionDismissed : SavingsGoalDetailEvent
    data object MarkCompleteClicked : SavingsGoalDetailEvent
}

sealed interface SavingsGoalDetailEffect : UiEffect {
    data class NavigateToEdit(val id: String) : SavingsGoalDetailEffect
    data object NavigateBack : SavingsGoalDetailEffect
    data class ShowMessage(val message: String) : SavingsGoalDetailEffect
}
