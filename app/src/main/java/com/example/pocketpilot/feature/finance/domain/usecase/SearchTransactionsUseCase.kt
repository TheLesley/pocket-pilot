package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionQuery
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Stream-based search + filter use case.
 *
 * The ViewModel exposes a hot [Flow] of [TransactionQuery] (search bar edits,
 * chip toggles, filter-sheet applies) and this use case:
 *  1. Debounces text-driven edits so we don't hammer Room on every keystroke.
 *  2. Deduplicates identical consecutive queries.
 *  3. Uses `flatMapLatest` so an in-flight subscription is cancelled and
 *     replaced whenever the query changes.
 *
 * Keeping this logic in the domain layer means every consumer that needs
 * "search transactions live" gets the same debounce semantics for free.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchTransactionsUseCase(private val repository: TransactionRepository, private val debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS) {
    operator fun invoke(queries: Flow<TransactionQuery>): Flow<List<Transaction>> = queries
        .debounce { query -> if (query.searchTerm.isBlank()) 0L else debounceMillis }
        .distinctUntilChanged()
        .flatMapLatest { repository.observe(it) }

    companion object {
        const val DEFAULT_DEBOUNCE_MILLIS: Long = 250L
    }
}
