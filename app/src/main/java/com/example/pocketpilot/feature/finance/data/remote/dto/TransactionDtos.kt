package com.example.pocketpilot.feature.finance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionTypeDto {
    @SerialName("income")
    INCOME,

    @SerialName("expense")
    EXPENSE
}

@Serializable
data class TransactionDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("amount_minor_units") val amountMinorUnits: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("type") val type: TransactionTypeDto,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("budget_id") val budgetId: String? = null,
    @SerialName("occurred_at") val occurredAtEpochMillis: Long,
    @SerialName("note") val note: String? = null,
    @SerialName("created_at") val createdAtEpochMillis: Long,
    @SerialName("updated_at") val updatedAtEpochMillis: Long
)

@Serializable
data class TransactionListResponseDto(
    @SerialName("items") val items: List<TransactionDto>,
    @SerialName("next_cursor") val nextCursor: String? = null
)

@Serializable
data class CreateTransactionRequestDto(
    @SerialName("title") val title: String,
    @SerialName("amount_minor_units") val amountMinorUnits: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("type") val type: TransactionTypeDto,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("budget_id") val budgetId: String? = null,
    @SerialName("occurred_at") val occurredAtEpochMillis: Long,
    @SerialName("note") val note: String? = null
)

@Serializable
data class UpdateTransactionRequestDto(
    @SerialName("title") val title: String,
    @SerialName("amount_minor_units") val amountMinorUnits: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("type") val type: TransactionTypeDto,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("budget_id") val budgetId: String? = null,
    @SerialName("occurred_at") val occurredAtEpochMillis: Long,
    @SerialName("note") val note: String? = null
)
