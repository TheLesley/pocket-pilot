package com.example.pocketpilot.feature.finance.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Budget(
    val id: String,
    val name: String,
    val limitMinorUnits: Long,
    val currencyCode: String,
    val period: BudgetPeriod,
    val startsAtEpochMillis: Long,
    val endsAtEpochMillis: Long?,
    val categoryId: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

enum class BudgetPeriod { WEEKLY, MONTHLY, YEARLY, CUSTOM }
