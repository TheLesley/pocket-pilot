package com.example.pocketpilot.feature.finance.data.local.query

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.pocketpilot.feature.finance.domain.model.SortDirection
import com.example.pocketpilot.feature.finance.domain.model.TransactionQuery
import com.example.pocketpilot.feature.finance.domain.model.TransactionSortField

/**
 * Translates a domain [TransactionQuery] into a parameterised SQLite statement.
 *
 * Every user-supplied value is bound with `?` — column identifiers and sort
 * clauses are the only things concatenated into the raw SQL, so search terms /
 * amount thresholds cannot be used to inject arbitrary SQL.
 */
internal object TransactionQuerySqlBuilder {

    fun build(query: TransactionQuery): SupportSQLiteQuery {
        val clauses = mutableListOf("sync_status != 'PENDING_DELETE'")
        val args = mutableListOf<Any>()

        val term = query.searchTerm.trim()
        if (term.isNotEmpty()) {
            clauses += "(LOWER(title) LIKE ? ESCAPE '\\' OR LOWER(IFNULL(note, '')) LIKE ? ESCAPE '\\')"
            val like = "%${term.lowercase().escapeLikeWildcards()}%"
            args += like
            args += like
        }

        if (query.types.isNotEmpty()) {
            clauses += "type IN (${query.types.joinToString(",") { "?" }})"
            query.types.forEach { args += it.name }
        }

        if (query.categoryIds.isNotEmpty()) {
            clauses += "category_id IN (${query.categoryIds.joinToString(",") { "?" }})"
            query.categoryIds.forEach { args += it }
        }

        query.fromEpochMillis?.let {
            clauses += "occurred_at >= ?"
            args += it
        }
        query.toEpochMillis?.let {
            clauses += "occurred_at <= ?"
            args += it
        }
        query.minAmountMinorUnits?.let {
            clauses += "amount_minor_units >= ?"
            args += it
        }
        query.maxAmountMinorUnits?.let {
            clauses += "amount_minor_units <= ?"
            args += it
        }

        val orderBy = buildOrderBy(query)
        val sql = buildString {
            append("SELECT * FROM transactions WHERE ")
            append(clauses.joinToString(" AND "))
            append(" ORDER BY ")
            append(orderBy)
        }
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    private fun buildOrderBy(query: TransactionQuery): String {
        val direction = when (query.sort.direction) {
            SortDirection.ASC -> "ASC"
            SortDirection.DESC -> "DESC"
        }
        // Every field falls back to `occurred_at DESC` so the ordering is
        // deterministic when the primary key ties (e.g. two rows with the same
        // amount).
        return when (query.sort.field) {
            TransactionSortField.DATE -> "occurred_at $direction, id ASC"
            TransactionSortField.AMOUNT -> "amount_minor_units $direction, occurred_at DESC"
            TransactionSortField.TITLE -> "LOWER(title) $direction, occurred_at DESC"
        }
    }

    private fun String.escapeLikeWildcards(): String = replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
}
