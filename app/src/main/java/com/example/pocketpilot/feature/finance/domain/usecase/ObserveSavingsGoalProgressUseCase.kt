package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.SavingsGoalProgress
import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveSavingsGoalProgressUseCase(
    private val repository: SavingsGoalRepository,
    private val now: () -> Long = System::currentTimeMillis
) {
    operator fun invoke(id: String): Flow<SavingsGoalProgress?> = repository.observeById(id).map { goal ->
        goal?.let { SavingsGoalProgress(goal = it, nowEpochMillis = now()) }
    }
}
