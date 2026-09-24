package com.example.pocketpilot.feature.finance.presentation.savings.list

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveSavingsGoalsProgressUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

class SavingsGoalListViewModel(private val observeSavingsGoalsProgressUseCase: ObserveSavingsGoalsProgressUseCase) :
    BaseViewModel<SavingsGoalListState, SavingsGoalListEvent, SavingsGoalListEffect>(
        SavingsGoalListState()
    ) {

    private var observeJob: Job? = null

    init {
        observeGoals()
    }

    override fun handleEvent(event: SavingsGoalListEvent) {
        when (event) {
            SavingsGoalListEvent.AddClicked -> sendEffect(SavingsGoalListEffect.NavigateToAdd)
            is SavingsGoalListEvent.GoalClicked ->
                sendEffect(SavingsGoalListEffect.NavigateToDetail(event.id))
            SavingsGoalListEvent.Retry -> observeGoals()
        }
    }

    private fun observeGoals() {
        observeJob?.cancel()
        observeJob = observeSavingsGoalsProgressUseCase()
            .onStart { setState { copy(goals = UiState.Loading) } }
            .catch { t ->
                setState {
                    copy(
                        goals = UiState.Error(
                            message = t.message ?: "Failed to load savings goals",
                            throwable = t
                        )
                    )
                }
            }
            .onEach { items ->
                setState {
                    copy(
                        goals = if (items.isEmpty()) UiState.Empty else UiState.Success(items)
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
