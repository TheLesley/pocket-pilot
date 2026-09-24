package com.example.pocketpilot.feature.finance.data.mapper

import com.example.pocketpilot.feature.finance.data.remote.dto.BudgetDto
import com.example.pocketpilot.feature.finance.data.remote.dto.BudgetPeriodDto
import com.example.pocketpilot.feature.finance.data.remote.dto.CreateBudgetRequestDto
import com.example.pocketpilot.feature.finance.data.remote.dto.CreateSavingsGoalRequestDto
import com.example.pocketpilot.feature.finance.data.remote.dto.CreateTransactionRequestDto
import com.example.pocketpilot.feature.finance.data.remote.dto.SavingsGoalDto
import com.example.pocketpilot.feature.finance.data.remote.dto.TransactionDto
import com.example.pocketpilot.feature.finance.data.remote.dto.TransactionTypeDto
import com.example.pocketpilot.feature.finance.data.remote.dto.UpdateBudgetRequestDto
import com.example.pocketpilot.feature.finance.data.remote.dto.UpdateSavingsGoalRequestDto
import com.example.pocketpilot.feature.finance.data.remote.dto.UpdateTransactionRequestDto
import com.example.pocketpilot.feature.finance.domain.model.Budget
import com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

// -- Transactions --------------------------------------------------------

fun TransactionDto.toDomain(): Transaction = Transaction(
    id = id,
    title = title,
    amountMinorUnits = amountMinorUnits,
    currencyCode = currencyCode,
    type = type.toDomain(),
    categoryId = categoryId,
    budgetId = budgetId,
    occurredAtEpochMillis = occurredAtEpochMillis,
    note = note,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun Transaction.toCreateRequestDto(): CreateTransactionRequestDto = CreateTransactionRequestDto(
    title = title,
    amountMinorUnits = amountMinorUnits,
    currencyCode = currencyCode,
    type = type.toDto(),
    categoryId = categoryId,
    budgetId = budgetId,
    occurredAtEpochMillis = occurredAtEpochMillis,
    note = note
)

fun Transaction.toUpdateRequestDto(): UpdateTransactionRequestDto = UpdateTransactionRequestDto(
    title = title,
    amountMinorUnits = amountMinorUnits,
    currencyCode = currencyCode,
    type = type.toDto(),
    categoryId = categoryId,
    budgetId = budgetId,
    occurredAtEpochMillis = occurredAtEpochMillis,
    note = note
)

fun TransactionTypeDto.toDomain(): TransactionType = when (this) {
    TransactionTypeDto.INCOME -> TransactionType.INCOME
    TransactionTypeDto.EXPENSE -> TransactionType.EXPENSE
}

fun TransactionType.toDto(): TransactionTypeDto = when (this) {
    TransactionType.INCOME -> TransactionTypeDto.INCOME
    TransactionType.EXPENSE -> TransactionTypeDto.EXPENSE
}

// -- Budgets -------------------------------------------------------------

fun BudgetDto.toDomain(): Budget = Budget(
    id = id,
    name = name,
    limitMinorUnits = limitMinorUnits,
    currencyCode = currencyCode,
    period = period.toDomain(),
    startsAtEpochMillis = startsAtEpochMillis,
    endsAtEpochMillis = endsAtEpochMillis,
    categoryId = categoryId,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun Budget.toCreateRequestDto(): CreateBudgetRequestDto = CreateBudgetRequestDto(
    name = name,
    limitMinorUnits = limitMinorUnits,
    currencyCode = currencyCode,
    period = period.toDto(),
    startsAtEpochMillis = startsAtEpochMillis,
    endsAtEpochMillis = endsAtEpochMillis,
    categoryId = categoryId
)

fun Budget.toUpdateRequestDto(): UpdateBudgetRequestDto = UpdateBudgetRequestDto(
    name = name,
    limitMinorUnits = limitMinorUnits,
    currencyCode = currencyCode,
    period = period.toDto(),
    startsAtEpochMillis = startsAtEpochMillis,
    endsAtEpochMillis = endsAtEpochMillis,
    categoryId = categoryId
)

fun BudgetPeriodDto.toDomain(): BudgetPeriod = when (this) {
    BudgetPeriodDto.WEEKLY -> BudgetPeriod.WEEKLY
    BudgetPeriodDto.MONTHLY -> BudgetPeriod.MONTHLY
    BudgetPeriodDto.YEARLY -> BudgetPeriod.YEARLY
    BudgetPeriodDto.CUSTOM -> BudgetPeriod.CUSTOM
}

fun BudgetPeriod.toDto(): BudgetPeriodDto = when (this) {
    BudgetPeriod.WEEKLY -> BudgetPeriodDto.WEEKLY
    BudgetPeriod.MONTHLY -> BudgetPeriodDto.MONTHLY
    BudgetPeriod.YEARLY -> BudgetPeriodDto.YEARLY
    BudgetPeriod.CUSTOM -> BudgetPeriodDto.CUSTOM
}

// -- Savings goals -------------------------------------------------------

fun SavingsGoalDto.toDomain(): SavingsGoal = SavingsGoal(
    id = id,
    name = name,
    targetMinorUnits = targetMinorUnits,
    savedMinorUnits = savedMinorUnits,
    currencyCode = currencyCode,
    targetDateEpochMillis = targetDateEpochMillis,
    note = note,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun SavingsGoal.toCreateRequestDto(): CreateSavingsGoalRequestDto = CreateSavingsGoalRequestDto(
    name = name,
    targetMinorUnits = targetMinorUnits,
    savedMinorUnits = savedMinorUnits,
    currencyCode = currencyCode,
    targetDateEpochMillis = targetDateEpochMillis,
    note = note
)

fun SavingsGoal.toUpdateRequestDto(): UpdateSavingsGoalRequestDto = UpdateSavingsGoalRequestDto(
    name = name,
    targetMinorUnits = targetMinorUnits,
    savedMinorUnits = savedMinorUnits,
    currencyCode = currencyCode,
    targetDateEpochMillis = targetDateEpochMillis,
    note = note
)
