package com.example.pocketpilot.feature.analytics.domain.model

import androidx.compose.runtime.Immutable

/**
 * Category-level slice used by the breakdown donut chart. [share] is a value in
 * `[0.0, 1.0]` representing the fraction of the total expense — precomputed so
 * the UI can render pie/donut segments without doing division.
 */
@Immutable
data class CategoryShare(
    val categoryId: String?,
    val label: String,
    val amountMinorUnits: Long,
    val share: Float,
    val transactionCount: Int
)

/**
 * Full category distribution for a [DateRange]. Sorted descending by amount so
 * the "biggest slice" is always first.
 */
@Immutable
data class CategoryDistribution(val range: DateRange, val totalExpenseMinorUnits: Long, val shares: List<CategoryShare>) {
    val isEmpty: Boolean get() = totalExpenseMinorUnits == 0L || shares.isEmpty()
}
