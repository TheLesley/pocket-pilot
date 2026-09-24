package com.example.pocketpilot.feature.analytics.domain.model

import androidx.compose.runtime.Immutable

/**
 * One bucket in a trend series (e.g. a month in a monthly chart, or a year in a
 * yearly chart). [key] identifies the bucket in a chart-friendly form
 * (`2026-03` / `2026`); [label] is a short, human-readable variant intended for
 * axis ticks. Amounts stay in minor units and are always non-negative.
 */
@Immutable
data class TrendPoint(val key: String, val label: String, val incomeMinorUnits: Long, val expenseMinorUnits: Long) {
    val netMinorUnits: Long get() = incomeMinorUnits - expenseMinorUnits
}

enum class TrendGranularity { MONTHLY, YEARLY }

/**
 * Ordered trend series for a [DateRange]. The list is guaranteed to be sorted
 * chronologically and contains one entry per bucket in the window, even when
 * the bucket had no activity — this keeps chart x-axes evenly spaced.
 */
@Immutable
data class TrendSeries(val granularity: TrendGranularity, val range: DateRange, val points: List<TrendPoint>) {
    val maxAmountMinorUnits: Long
        get() = points.maxOfOrNull {
            maxOf(it.incomeMinorUnits, it.expenseMinorUnits)
        } ?: 0L

    val isEmpty: Boolean get() = points.all { it.incomeMinorUnits == 0L && it.expenseMinorUnits == 0L }
}
