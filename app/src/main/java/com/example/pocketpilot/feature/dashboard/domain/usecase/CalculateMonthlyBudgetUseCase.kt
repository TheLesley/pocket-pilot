package com.example.pocketpilot.feature.dashboard.domain.usecase

import com.example.pocketpilot.feature.dashboard.domain.model.MonthRange
import com.example.pocketpilot.feature.finance.domain.model.Budget
import com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod

/**
 * Aggregates every active budget for the given month and returns the total
 * monthly-equivalent limit. Weekly / yearly / custom budgets are normalised
 * so the dashboard can display a single "remaining this month" figure without
 * asking the user to reason about mixed periods.
 *
 * Normalisation:
 * - `MONTHLY` → limit as-is.
 * - `WEEKLY` → limit × (average weeks in a month, 52/12).
 * - `YEARLY` → limit ÷ 12.
 * - `CUSTOM` → limit prorated to the fraction of its span overlapping the
 *   current month (falls back to the raw limit when no end date is defined).
 *
 * Budgets whose active window (`startsAt` … `endsAt`) does not overlap the
 * requested month are excluded.
 */
class CalculateMonthlyBudgetUseCase {

    operator fun invoke(budgets: List<Budget>, range: MonthRange): Long {
        if (budgets.isEmpty()) return 0L
        val monthMillis = (range.toEpochMillis - range.fromEpochMillis).coerceAtLeast(1L)
        var total = 0.0
        for (budget in budgets) {
            if (!budget.overlaps(range)) continue
            total += when (budget.period) {
                BudgetPeriod.MONTHLY -> budget.limitMinorUnits.toDouble()
                BudgetPeriod.WEEKLY -> budget.limitMinorUnits.toDouble() * WEEKS_PER_MONTH
                BudgetPeriod.YEARLY -> budget.limitMinorUnits.toDouble() / MONTHS_PER_YEAR
                BudgetPeriod.CUSTOM -> budget.proratedMonthly(monthMillis)
            }
        }
        return total.toLong()
    }

    private fun Budget.overlaps(range: MonthRange): Boolean {
        val end = endsAtEpochMillis ?: Long.MAX_VALUE
        return startsAtEpochMillis < range.toEpochMillis && end >= range.fromEpochMillis
    }

    private fun Budget.proratedMonthly(monthMillis: Long): Double {
        val end = endsAtEpochMillis ?: return limitMinorUnits.toDouble()
        val span = (end - startsAtEpochMillis).coerceAtLeast(1L)
        return limitMinorUnits.toDouble() * (monthMillis.toDouble() / span.toDouble())
    }

    private companion object {
        const val WEEKS_PER_MONTH = 52.0 / 12.0
        const val MONTHS_PER_YEAR = 12.0
    }
}
