package com.example.pocketpilot.feature.finance.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavingsGoalDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("target_minor_units") val targetMinorUnits: Long,
    @SerialName("saved_minor_units") val savedMinorUnits: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("target_date") val targetDateEpochMillis: Long? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("created_at") val createdAtEpochMillis: Long,
    @SerialName("updated_at") val updatedAtEpochMillis: Long
)

@Serializable
data class SavingsGoalListResponseDto(@SerialName("items") val items: List<SavingsGoalDto>)

@Serializable
data class CreateSavingsGoalRequestDto(
    @SerialName("name") val name: String,
    @SerialName("target_minor_units") val targetMinorUnits: Long,
    @SerialName("saved_minor_units") val savedMinorUnits: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("target_date") val targetDateEpochMillis: Long? = null,
    @SerialName("note") val note: String? = null
)

@Serializable
data class UpdateSavingsGoalRequestDto(
    @SerialName("name") val name: String,
    @SerialName("target_minor_units") val targetMinorUnits: Long,
    @SerialName("saved_minor_units") val savedMinorUnits: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("target_date") val targetDateEpochMillis: Long? = null,
    @SerialName("note") val note: String? = null
)

@Serializable
data class AdjustSavingsContributionRequestDto(@SerialName("delta_minor_units") val deltaMinorUnits: Long)
