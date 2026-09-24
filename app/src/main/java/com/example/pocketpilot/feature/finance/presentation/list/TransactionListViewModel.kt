package com.example.pocketpilot.feature.finance.presentation.list

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.finance.domain.usecase.SearchTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

class TransactionListViewModel(private val searchTransactionsUseCase: SearchTransactionsUseCase) :
    BaseViewModel<TransactionListState, TransactionListEvent, TransactionListEffect>(TransactionListState()) {

    private val queryFlow = MutableStateFlow(currentState.toQuery())

    init {
        observeQueryChanges()
    }

    override fun handleEvent(event: TransactionListEvent) {
        when (event) {
            is TransactionListEvent.SearchChanged -> updateQuery { copy(searchTerm = event.value) }
            is TransactionListEvent.TypeFilterToggled -> updateQuery {
                copy(typeFilters = typeFilters.toggle(event.type))
            }
            is TransactionListEvent.CategoryFilterToggled -> updateQuery {
                copy(categoryFilters = categoryFilters.toggle(event.categoryId))
            }
            is TransactionListEvent.DateRangeChanged -> updateQuery {
                copy(fromDateMillis = event.fromMillis, toDateMillis = event.toMillis)
            }
            is TransactionListEvent.AmountRangeChanged -> updateQuery {
                copy(
                    minAmountMinorUnits = event.minMinorUnits,
                    maxAmountMinorUnits = event.maxMinorUnits
                )
            }
            is TransactionListEvent.SortChanged -> updateQuery {
                copy(sortField = event.field, sortDirection = event.direction)
            }
            TransactionListEvent.OpenFilterSheet -> setState { copy(filterSheetVisible = true) }
            TransactionListEvent.DismissFilterSheet -> setState { copy(filterSheetVisible = false) }
            TransactionListEvent.ClearFilters -> updateQuery {
                copy(
                    searchTerm = "",
                    typeFilters = emptySet(),
                    categoryFilters = emptySet(),
                    fromDateMillis = null,
                    toDateMillis = null,
                    minAmountMinorUnits = null,
                    maxAmountMinorUnits = null
                )
            }
            TransactionListEvent.ClearSearch -> updateQuery { copy(searchTerm = "") }
            TransactionListEvent.AddClicked -> sendEffect(TransactionListEffect.NavigateToAdd)
            is TransactionListEvent.TransactionClicked -> sendEffect(
                TransactionListEffect.NavigateToDetail(event.id)
            )
            TransactionListEvent.Retry -> queryFlow.value = currentState.toQuery()
        }
    }

    private fun updateQuery(reducer: TransactionListState.() -> TransactionListState) {
        setState(reducer)
        queryFlow.value = currentState.toQuery()
    }

    private fun observeQueryChanges() {
        searchTransactionsUseCase(queryFlow)
            .onStart { setState { copy(transactions = UiState.Loading) } }
            .catch { t ->
                setState {
                    copy(
                        transactions = UiState.Error(
                            message = t.message ?: "Failed to load transactions",
                            throwable = t
                        )
                    )
                }
            }
            .onEach { items ->
                setState {
                    copy(
                        transactions = if (items.isEmpty()) UiState.Empty else UiState.Success(items),
                        availableCategoryIds = mergedCategoryIds(items)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Union of the currently-selected category filters and any category IDs
     * present in the latest result set. Selecting a category and then typing a
     * search term that hides every row of that category must not make the chip
     * disappear from the filter sheet.
     */
    private fun TransactionListState.mergedCategoryIds(
        results: List<com.example.pocketpilot.feature.finance.domain.model.Transaction>
    ): List<String> {
        val fromResults = results.mapNotNull { it.categoryId }
        return (fromResults + categoryFilters + availableCategoryIds)
            .toSortedSet()
            .toList()
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (contains(value)) this - value else this + value
