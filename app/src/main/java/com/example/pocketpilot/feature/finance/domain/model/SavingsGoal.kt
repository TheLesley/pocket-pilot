package com.example.pocketpilot.feature.finance.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class SavingsGoal(
    val id: String,
    val name: String,
    val targetMinorUnits: Long,
    val savedMinorUnits: Long,
    val currencyCode: String,
    val targetDateEpochMillis: Long?,
    val note: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
