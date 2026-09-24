package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository

/**
 * Marks a savings goal as fully reached by topping up its saved balance to the
 * target amount. Returns [Result.AlreadyComplete] when nothing needs to change.
 */
class MarkSavingsGoalCompleteUseCase(
    private val repository: SavingsGoalRepository,
    private val now: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(id: String): Result {
        val goal = repository.getById(id) ?: return Result.NotFound
        if (goal.savedMinorUnits >= goal.targetMinorUnits) return Result.AlreadyComplete
        repository.upsert(
            goal.copy(
                savedMinorUnits = goal.targetMinorUnits,
                updatedAtEpochMillis = now()
            )
        )
        return Result.Completed
    }

    sealed interface Result {
        data object Completed : Result
        data object AlreadyComplete : Result
        data object NotFound : Result
    }
}
