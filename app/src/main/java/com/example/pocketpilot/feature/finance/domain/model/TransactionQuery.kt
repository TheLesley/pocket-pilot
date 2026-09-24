package com.example.pocketpilot.feature.finance.domain.model

import androidx.compose.runtime.Immutable

/**
 * Immutable, self-describing search + filter descriptor consumed by the
 * repository and use cases. Every field is optional; a bare `TransactionQuery()`
 * observes every non-deleted transaction sorted by most recent occurrence.
 *
 * Multi-value fields (`types`, `categoryIds`) are `Set`s: empty means "no
 * constraint on this facet" so the UI can add/remove chips freely without
 * needing null-handling.
 */
@Immutable
data class TransactionQuery(
    val searchTerm: String = "",
    val types: Set<TransactionType> = emptySet(),
    val categoryIds: Set<String> = emptySet(),
    val fromEpochMillis: Long? = null,
    val toEpochMillis: Long? = null,
    val minAmountMinorUnits: Long? = null,
    val maxAmountMinorUnits: Long? = null,
    val sort: TransactionSort = TransactionSort.Default
) {
    val hasActiveFilters: Boolean
        get() = searchTerm.isNotBlank() ||
            types.isNotEmpty() ||
            categoryIds.isNotEmpty() ||
            fromEpochMillis != null ||
            toEpochMillis != null ||
            minAmountMinorUnits != null ||
            maxAmountMinorUnits != null
}

@Immutable
data class TransactionSort(val field: TransactionSortField = TransactionSortField.DATE, val direction: SortDirection = SortDirection.DESC) {
    companion object {
        val Default = TransactionSort()
    }
}

enum class TransactionSortField { DATE, AMOUNT, TITLE }

enum class SortDirection { ASC, DESC }
