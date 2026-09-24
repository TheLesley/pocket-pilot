package com.example.pocketpilot.feature.finance.presentation.list

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.finance.domain.model.SortDirection
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionQuery
import com.example.pocketpilot.feature.finance.domain.model.TransactionSort
import com.example.pocketpilot.feature.finance.domain.model.TransactionSortField
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

/**
 * UDF state for the transaction list. Filter facets are stored explicitly so
 * the UI can render "active filter chips" without having to re-parse a query
 * object every recomposition.
 */
@Immutable
data class TransactionListState(
    val transactions: UiState<List<Transaction>> = UiState.Loading,
    val availableCategoryIds: List<String> = emptyList(),
    val searchTerm: String = "",
    val typeFilters: Set<TransactionType> = emptySet(),
    val categoryFilters: Set<String> = emptySet(),
    val fromDateMillis: Long? = null,
    val toDateMillis: Long? = null,
    val minAmountMinorUnits: Long? = null,
    val maxAmountMinorUnits: Long? = null,
    val sortField: TransactionSortField = TransactionSortField.DATE,
    val sortDirection: SortDirection = SortDirection.DESC,
    val filterSheetVisible: Boolean = false
) {
    val hasActiveFilters: Boolean
        get() = searchTerm.isNotBlank() ||
            typeFilters.isNotEmpty() ||
            categoryFilters.isNotEmpty() ||
            fromDateMillis != null ||
            toDateMillis != null ||
            minAmountMinorUnits != null ||
            maxAmountMinorUnits != null

    val activeFilterCount: Int
        get() = listOf(
            searchTerm.isNotBlank(),
            typeFilters.isNotEmpty(),
            categoryFilters.isNotEmpty(),
            fromDateMillis != null || toDateMillis != null,
            minAmountMinorUnits != null || maxAmountMinorUnits != null
        ).count { it }

    fun toQuery(): TransactionQuery = TransactionQuery(
        searchTerm = searchTerm,
        types = typeFilters,
        categoryIds = categoryFilters,
        fromEpochMillis = fromDateMillis,
        toEpochMillis = toDateMillis,
        minAmountMinorUnits = minAmountMinorUnits,
        maxAmountMinorUnits = maxAmountMinorUnits,
        sort = TransactionSort(field = sortField, direction = sortDirection)
    )
}

sealed interface TransactionListEvent : UiEvent {
    data class SearchChanged(val value: String) : TransactionListEvent
    data class TypeFilterToggled(val type: TransactionType) : TransactionListEvent
    data class CategoryFilterToggled(val categoryId: String) : TransactionListEvent
    data class DateRangeChanged(val fromMillis: Long?, val toMillis: Long?) : TransactionListEvent
    data class AmountRangeChanged(val minMinorUnits: Long?, val maxMinorUnits: Long?) : TransactionListEvent
    data class SortChanged(val field: TransactionSortField, val direction: SortDirection) : TransactionListEvent
    data object OpenFilterSheet : TransactionListEvent
    data object DismissFilterSheet : TransactionListEvent
    data object ClearFilters : TransactionListEvent
    data object ClearSearch : TransactionListEvent
    data object AddClicked : TransactionListEvent
    data class TransactionClicked(val id: String) : TransactionListEvent
    data object Retry : TransactionListEvent
}

sealed interface TransactionListEffect : UiEffect {
    data object NavigateToAdd : TransactionListEffect
    data class NavigateToDetail(val id: String) : TransactionListEffect
}
