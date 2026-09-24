package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.SavingsGoalProgress
import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Wraps [SavingsGoal] items with [SavingsGoalProgress] so the presentation
 * layer receives ratio/status/days-remaining without duplicating logic.
 * The `now` supplier is injectable to keep the use case deterministic in tests.
 */
class ObserveSavingsGoalsProgressUseCase(
    private val repository: SavingsGoalRepository,
    private val now: () -> Long = System::currentTimeMillis
) {
    operator fun invoke(): Flow<List<SavingsGoalProgress>> = repository.observeAll().map { goals ->
        val currentMillis = now()
        goals.map { SavingsGoalProgress(goal = it, nowEpochMillis = currentMillis) }
    }
}
