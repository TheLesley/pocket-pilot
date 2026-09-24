package com.example.pocketpilot.feature.finance.presentation.budget.list

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveBudgetsProgressUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

class BudgetListViewModel(private val observeBudgetsProgressUseCase: ObserveBudgetsProgressUseCase) :
    BaseViewModel<BudgetListState, BudgetListEvent, BudgetListEffect>(BudgetListState()) {

    private var observeJob: Job? = null

    init {
        observeBudgets()
    }

    override fun handleEvent(event: BudgetListEvent) {
        when (event) {
            BudgetListEvent.AddClicked -> sendEffect(BudgetListEffect.NavigateToAdd)
            is BudgetListEvent.BudgetClicked ->
                sendEffect(BudgetListEffect.NavigateToDetail(event.id))
            BudgetListEvent.Retry -> observeBudgets()
        }
    }

    private fun observeBudgets() {
        observeJob?.cancel()
        observeJob = observeBudgetsProgressUseCase()
            .onStart { setState { copy(budgets = UiState.Loading) } }
            .catch { t ->
                setState {
                    copy(
                        budgets = UiState.Error(
                            message = t.message ?: "Failed to load budgets",
                            throwable = t
                        )
                    )
                }
            }
            .onEach { items ->
                setState {
                    copy(
                        budgets = if (items.isEmpty()) UiState.Empty else UiState.Success(items)
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
