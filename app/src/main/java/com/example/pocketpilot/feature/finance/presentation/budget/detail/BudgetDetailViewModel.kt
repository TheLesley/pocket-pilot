package com.example.pocketpilot.feature.finance.presentation.budget.detail

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.finance.domain.usecase.DeleteBudgetUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveBudgetProgressUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class BudgetDetailViewModel(
    private val budgetId: String,
    private val observeBudgetProgressUseCase: ObserveBudgetProgressUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase
) : BaseViewModel<BudgetDetailState, BudgetDetailEvent, BudgetDetailEffect>(BudgetDetailState()) {

    init {
        observeBudget()
    }

    override fun handleEvent(event: BudgetDetailEvent) {
        when (event) {
            BudgetDetailEvent.EditClicked ->
                sendEffect(BudgetDetailEffect.NavigateToEdit(budgetId))
            BudgetDetailEvent.DeleteClicked -> setState { copy(showDeleteConfirmation = true) }
            BudgetDetailEvent.DeleteDismissed -> setState { copy(showDeleteConfirmation = false) }
            BudgetDetailEvent.DeleteConfirmed -> delete()
            BudgetDetailEvent.BackClicked -> sendEffect(BudgetDetailEffect.NavigateBack)
            BudgetDetailEvent.Retry -> observeBudget()
        }
    }

    private fun observeBudget() {
        observeBudgetProgressUseCase(budgetId)
            .onStart { setState { copy(progress = UiState.Loading) } }
            .catch { t ->
                setState {
                    copy(
                        progress = UiState.Error(
                            message = t.message ?: "Failed to load budget",
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

    private fun delete() {
        setState { copy(isDeleting = true, showDeleteConfirmation = false) }
        viewModelScope.launch {
            runCatching { deleteBudgetUseCase(budgetId) }
                .onSuccess {
                    setState { copy(isDeleting = false) }
                    sendEffect(BudgetDetailEffect.NavigateBack)
                }
                .onFailure { t ->
                    setState { copy(isDeleting = false) }
                    sendEffect(BudgetDetailEffect.ShowError(t.message ?: "Delete failed"))
                }
        }
    }
}
