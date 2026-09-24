package com.example.pocketpilot.feature.finance.presentation.savings

import com.example.pocketpilot.feature.finance.domain.model.SavingsGoalProgress
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoalStatus

/**
 * Human-readable label for a countdown to a savings goal's target date. Returns
 * `null` when the goal has no deadline (caller can hide the row).
 */
internal fun SavingsGoalProgress.timeRemainingLabel(): String? {
    val days = daysRemaining ?: return null
    return when {
        isComplete -> "Goal reached"
        days < 0L -> {
            val overdue = -days
            if (overdue == 1L) "1 day overdue" else "$overdue days overdue"
        }
        days == 0L -> "Due today"
        days == 1L -> "1 day left"
        days < 60L -> "$days days left"
        days < 365L -> "${days / 30L} months left"
        else -> "${days / 365L} years left"
    }
}

internal fun SavingsGoalStatus.displayName(): String = when (this) {
    SavingsGoalStatus.ON_TRACK -> "On track"
    SavingsGoalStatus.AT_RISK -> "At risk"
    SavingsGoalStatus.OVERDUE -> "Overdue"
    SavingsGoalStatus.COMPLETED -> "Completed"
}
