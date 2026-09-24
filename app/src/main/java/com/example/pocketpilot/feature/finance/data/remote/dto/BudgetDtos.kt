package com.example.pocketpilot.feature.finance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BudgetPeriodDto {
    @SerialName("weekly")
    WEEKLY,

    @SerialName("monthly")
    MONTHLY,

    @SerialName("yearly")
    YEARLY,

    @SerialName("custom")
    CUSTOM
}

@Serializable
data class BudgetDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("limit_minor_units") val limitMinorUnits: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("period") val period: BudgetPeriodDto,
    @SerialName("starts_at") val startsAtEpochMillis: Long,
    @SerialName("ends_at") val endsAtEpochMillis: Long? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("created_at") val createdAtEpochMillis: Long,
    @SerialName("updated_at") val updatedAtEpochMillis: Long
)

@Serializable
data class BudgetListResponseDto(@SerialName("items") val items: List<BudgetDto>)

@Serializable
data class CreateBudgetRequestDto(
    @SerialName("name") val name: String,
    @SerialName("limit_minor_units") val limitMinorUnits: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("period") val period: BudgetPeriodDto,
    @SerialName("starts_at") val startsAtEpochMillis: Long,
    @SerialName("ends_at") val endsAtEpochMillis: Long? = null,
    @SerialName("category_id") val categoryId: String? = null
)

@Serializable
data class UpdateBudgetRequestDto(
    @SerialName("name") val name: String,
    @SerialName("limit_minor_units") val limitMinorUnits: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("period") val period: BudgetPeriodDto,
    @SerialName("starts_at") val startsAtEpochMillis: Long,
    @SerialName("ends_at") val endsAtEpochMillis: Long? = null,
    @SerialName("category_id") val categoryId: String? = null
)
