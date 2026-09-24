package com.example.pocketpilot.feature.finance.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Transaction(
    val id: String,
    val title: String,
    val amountMinorUnits: Long,
    val currencyCode: String,
    val type: TransactionType,
    val categoryId: String?,
    val budgetId: String?,
    val occurredAtEpochMillis: Long,
    val note: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

enum class TransactionType { INCOME, EXPENSE }
