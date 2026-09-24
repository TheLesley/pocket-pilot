package com.example.pocketpilot.feature.finance.domain.model

import androidx.compose.runtime.Immutable

/**
 * A [SavingsGoal] paired with derived progress info: how close the user is to
 * their target and how long they have to get there. Monetary fields are minor
 * units in the goal's currency; presentation formats via `MoneyFormatter`.
 */
@Immutable
data class SavingsGoalProgress(val goal: SavingsGoal, val nowEpochMillis: Long) {
    val targetMinorUnits: Long get() = goal.targetMinorUnits

    val savedMinorUnits: Long get() = goal.savedMinorUnits

    val remainingMinorUnits: Long
        get() = (targetMinorUnits - savedMinorUnits).coerceAtLeast(0L)

    /**
     * Fraction of target reached in `[0f, 1f]`. Zero target (misconfigured
     * goal) is treated as complete when anything is saved so the UI shows a
     * full bar instead of dividing by zero.
     */
    val ratio: Float
        get() = when {
            targetMinorUnits <= 0L -> if (savedMinorUnits > 0L) 1f else 0f
            savedMinorUnits >= targetMinorUnits -> 1f
            else -> savedMinorUnits.toFloat() / targetMinorUnits.toFloat()
        }

    val percent: Int get() = (ratio * 100f).toInt()

    val isComplete: Boolean
        get() = targetMinorUnits > 0L && savedMinorUnits >= targetMinorUnits

    /**
     * Whole days between now and the target date, or `null` if the goal has no
     * deadline. Negative values indicate the deadline has already passed.
     */
    val daysRemaining: Long?
        get() = goal.targetDateEpochMillis?.let { target ->
            val diff = target - nowEpochMillis
            diff / MILLIS_IN_DAY
        }

    val status: SavingsGoalStatus
        get() = when {
            isComplete -> SavingsGoalStatus.COMPLETED
            daysRemaining != null && daysRemaining!! < 0 -> SavingsGoalStatus.OVERDUE
            daysRemaining != null && daysRemaining!! <= AT_RISK_DAYS && ratio < AT_RISK_RATIO ->
                SavingsGoalStatus.AT_RISK
            else -> SavingsGoalStatus.ON_TRACK
        }

    companion object {
        const val AT_RISK_DAYS: Long = 14
        const val AT_RISK_RATIO: Float = 0.75f
        private const val MILLIS_IN_DAY: Long = 24L * 60L * 60L * 1000L
    }
}

enum class SavingsGoalStatus { ON_TRACK, AT_RISK, OVERDUE, COMPLETED }
