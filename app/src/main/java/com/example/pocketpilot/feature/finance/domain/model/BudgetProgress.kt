package com.example.pocketpilot.feature.finance.domain.model

import androidx.compose.runtime.Immutable

/**
 * A [Budget] paired with the amount currently spent against it. All monetary
 * values are minor units in the budget's currency; presentation is expected to
 * format via `MoneyFormatter`.
 */
@Immutable
data class BudgetProgress(val budget: Budget, val spentMinorUnits: Long) {
    val limitMinorUnits: Long get() = budget.limitMinorUnits

    val remainingMinorUnits: Long get() = limitMinorUnits - spentMinorUnits

    /**
     * Ratio of spent-to-limit as a fraction in `[0f, ∞)`. When the limit is
     * zero (a misconfigured budget) we surface `1f` so the UI shows a full
     * bar instead of dividing by zero.
     */
    val ratio: Float
        get() = when {
            limitMinorUnits <= 0L -> if (spentMinorUnits > 0L) 1f else 0f
            else -> spentMinorUnits.toFloat() / limitMinorUnits.toFloat()
        }

    val percent: Int get() = (ratio * 100f).toInt()

    val status: BudgetStatus
        get() = when {
            ratio >= 1f -> BudgetStatus.EXCEEDED
            ratio >= WARNING_THRESHOLD -> BudgetStatus.WARNING
            else -> BudgetStatus.ON_TRACK
        }

    companion object {
        const val WARNING_THRESHOLD: Float = 0.8f
    }
}

enum class BudgetStatus { ON_TRACK, WARNING, EXCEEDED }
