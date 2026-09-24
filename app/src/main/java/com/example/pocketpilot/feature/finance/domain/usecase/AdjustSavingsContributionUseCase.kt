package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository

/**
 * Adds or removes funds against a [SavingsGoal]. Positive amounts deposit,
 * negative amounts withdraw. The saved balance is clamped at zero so a
 * withdrawal larger than the current balance simply empties the goal instead
 * of going negative.
 */
class AdjustSavingsContributionUseCase(
    private val repository: SavingsGoalRepository,
    private val now: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(id: String, deltaMinorUnits: Long): Result {
        if (deltaMinorUnits == 0L) return Result.NoChange
        val goal = repository.getById(id) ?: return Result.NotFound
        val newSaved = (goal.savedMinorUnits + deltaMinorUnits).coerceAtLeast(0L)
        val effectiveDelta = newSaved - goal.savedMinorUnits
        if (effectiveDelta == 0L) return Result.NoChange
        val timestamp = now()
        repository.upsert(
            goal.copy(
                savedMinorUnits = newSaved,
                updatedAtEpochMillis = timestamp
            )
        )
        return Result.Applied(newSavedMinorUnits = newSaved)
    }

    sealed interface Result {
        data class Applied(val newSavedMinorUnits: Long) : Result
        data object NotFound : Result
        data object NoChange : Result
    }
}
