package com.example.pocketpilot.feature.finance.presentation.savings.detail

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoalProgress
import com.example.pocketpilot.feature.finance.domain.usecase.AdjustSavingsContributionUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.DeleteSavingsGoalUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.MarkSavingsGoalCompleteUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveSavingsGoalProgressUseCase
import com.example.pocketpilot.feature.finance.domain.validation.SavingsGoalValidator
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SavingsGoalDetailViewModel(
    private val goalId: String,
    private val observeProgress: ObserveSavingsGoalProgressUseCase,
    private val adjustContribution: AdjustSavingsContributionUseCase,
    private val markComplete: MarkSavingsGoalCompleteUseCase,
    private val deleteGoal: DeleteSavingsGoalUseCase
) : BaseViewModel<SavingsGoalDetailState, SavingsGoalDetailEvent, SavingsGoalDetailEffect>(
    SavingsGoalDetailState()
) {

    init {
        observeGoal()
    }

    override fun handleEvent(event: SavingsGoalDetailEvent) {
        when (event) {
            SavingsGoalDetailEvent.EditClicked ->
                sendEffect(SavingsGoalDetailEffect.NavigateToEdit(goalId))
            SavingsGoalDetailEvent.DeleteClicked ->
                setState { copy(showDeleteConfirmation = true) }
            SavingsGoalDetailEvent.DeleteDismissed ->
                setState { copy(showDeleteConfirmation = false) }
            SavingsGoalDetailEvent.DeleteConfirmed -> delete()
            SavingsGoalDetailEvent.BackClicked ->
                sendEffect(SavingsGoalDetailEffect.NavigateBack)
            SavingsGoalDetailEvent.Retry -> observeGoal()
            SavingsGoalDetailEvent.AddFundsClicked -> openContributionDialog(ContributionMode.Add)
            SavingsGoalDetailEvent.RemoveFundsClicked ->
                openContributionDialog(ContributionMode.Remove)
            is SavingsGoalDetailEvent.ContributionInputChanged -> setState {
                copy(contributionInput = event.value, contributionError = null)
            }
            SavingsGoalDetailEvent.ContributionDismissed -> setState {
                copy(
                    showContributionDialog = false,
                    contributionInput = "",
                    contributionError = null
                )
            }
            SavingsGoalDetailEvent.ContributionSubmitted -> submitContribution()
            SavingsGoalDetailEvent.MarkCompleteClicked -> markCompleteInternal()
        }
    }

    private fun observeGoal() {
        observeProgress(goalId)
            .onStart { setState { copy(progress = UiState.Loading) } }
            .catch { t ->
                setState {
                    copy(
                        progress = UiState.Error(
                            message = t.message ?: "Failed to load savings goal",
                            throwable = t
                        )
                    )
                }
            }
            .onEach { p ->
                setState {
                    copy(
                        progress = when (p) {
                            null -> UiState.Empty
                            else -> UiState.Success(p)
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun openContributionDialog(mode: ContributionMode) {
        setState {
            copy(
                showContributionDialog = true,
                contributionMode = mode,
                contributionInput = "",
                contributionError = null
            )
        }
    }

    private fun submitContribution() {
        val s = currentState
        val progress = (s.progress as? UiState.Success<SavingsGoalProgress>)?.data ?: return
        val error = SavingsGoalValidator.validateContribution(s.contributionInput)?.message
        if (error != null) {
            setState { copy(contributionError = error) }
            return
        }
        val minorUnits = MoneyFormatter.parseToMinorUnits(
            raw = s.contributionInput,
            currencyCode = progress.goal.currencyCode
        )
        if (minorUnits == null || minorUnits <= 0L) {
            setState { copy(contributionError = "Enter a valid amount") }
            return
        }
        val signedDelta = when (s.contributionMode) {
            ContributionMode.Add -> minorUnits
            ContributionMode.Remove -> -minorUnits
        }
        setState { copy(isSubmittingContribution = true) }
        viewModelScope.launch {
            val result = runCatching { adjustContribution(goalId, signedDelta) }
            result
                .onSuccess { outcome ->
                    setState {
                        copy(
                            isSubmittingContribution = false,
                            showContributionDialog = false,
                            contributionInput = "",
                            contributionError = null
                        )
                    }
                    val message = when (outcome) {
                        is AdjustSavingsContributionUseCase.Result.Applied ->
                            if (s.contributionMode == ContributionMode.Add) {
                                "Funds added"
                            } else {
                                "Funds removed"
                            }
                        AdjustSavingsContributionUseCase.Result.NoChange -> "No change applied"
                        AdjustSavingsContributionUseCase.Result.NotFound -> "Goal not found"
                    }
                    sendEffect(SavingsGoalDetailEffect.ShowMessage(message))
                }
                .onFailure { t ->
                    setState {
                        copy(
                            isSubmittingContribution = false,
                            contributionError = t.message ?: "Failed to update contribution"
                        )
                    }
                }
        }
    }

    private fun markCompleteInternal() {
        if (currentState.progress !is UiState.Success) return
        setState { copy(isMarkingComplete = true) }
        viewModelScope.launch {
            val result = runCatching { markComplete(goalId) }
            result
                .onSuccess { outcome ->
                    setState { copy(isMarkingComplete = false) }
                    val message = when (outcome) {
                        MarkSavingsGoalCompleteUseCase.Result.Completed -> "Goal marked complete"
                        MarkSavingsGoalCompleteUseCase.Result.AlreadyComplete -> "Already complete"
                        MarkSavingsGoalCompleteUseCase.Result.NotFound -> "Goal not found"
                    }
                    sendEffect(SavingsGoalDetailEffect.ShowMessage(message))
                }
                .onFailure { t ->
                    setState { copy(isMarkingComplete = false) }
                    sendEffect(
                        SavingsGoalDetailEffect.ShowMessage(
                            t.message ?: "Failed to mark complete"
                        )
                    )
                }
        }
    }

    private fun delete() {
        setState { copy(isDeleting = true, showDeleteConfirmation = false) }
        viewModelScope.launch {
            runCatching { deleteGoal(goalId) }
                .onSuccess {
                    setState { copy(isDeleting = false) }
                    sendEffect(SavingsGoalDetailEffect.NavigateBack)
                }
                .onFailure { t ->
                    setState { copy(isDeleting = false) }
                    sendEffect(
                        SavingsGoalDetailEffect.ShowMessage(t.message ?: "Delete failed")
                    )
                }
        }
    }
}
