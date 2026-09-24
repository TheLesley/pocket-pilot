package com.example.pocketpilot.feature.finance.data.mapper

import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.data.local.entity.BudgetEntity
import com.example.pocketpilot.feature.finance.data.local.entity.SavingsGoalEntity
import com.example.pocketpilot.feature.finance.data.local.entity.TransactionEntity
import com.example.pocketpilot.feature.finance.domain.model.Budget
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal
import com.example.pocketpilot.feature.finance.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    title = title,
    amountMinorUnits = amountMinorUnits,
    currencyCode = currencyCode,
    type = type,
    categoryId = categoryId,
    budgetId = budgetId,
    occurredAtEpochMillis = occurredAtEpochMillis,
    note = note,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun Transaction.toEntity(
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    localUpdatedAtEpochMillis: Long = 0L,
    lastSyncError: String? = null
): TransactionEntity = TransactionEntity(
    id = id,
    title = title,
    amountMinorUnits = amountMinorUnits,
    currencyCode = currencyCode,
    type = type,
    categoryId = categoryId,
    budgetId = budgetId,
    occurredAtEpochMillis = occurredAtEpochMillis,
    note = note,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    syncStatus = syncStatus,
    localUpdatedAtEpochMillis = localUpdatedAtEpochMillis,
    lastSyncError = lastSyncError
)

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    name = name,
    limitMinorUnits = limitMinorUnits,
    currencyCode = currencyCode,
    period = period,
    startsAtEpochMillis = startsAtEpochMillis,
    endsAtEpochMillis = endsAtEpochMillis,
    categoryId = categoryId,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun Budget.toEntity(
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    localUpdatedAtEpochMillis: Long = 0L,
    lastSyncError: String? = null
): BudgetEntity = BudgetEntity(
    id = id,
    name = name,
    limitMinorUnits = limitMinorUnits,
    currencyCode = currencyCode,
    period = period,
    startsAtEpochMillis = startsAtEpochMillis,
    endsAtEpochMillis = endsAtEpochMillis,
    categoryId = categoryId,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    syncStatus = syncStatus,
    localUpdatedAtEpochMillis = localUpdatedAtEpochMillis,
    lastSyncError = lastSyncError
)

fun SavingsGoalEntity.toDomain(): SavingsGoal = SavingsGoal(
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

fun SavingsGoal.toEntity(
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    localUpdatedAtEpochMillis: Long = 0L,
    lastSyncError: String? = null
): SavingsGoalEntity = SavingsGoalEntity(
    id = id,
    name = name,
    targetMinorUnits = targetMinorUnits,
    savedMinorUnits = savedMinorUnits,
    currencyCode = currencyCode,
    targetDateEpochMillis = targetDateEpochMillis,
    note = note,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    syncStatus = syncStatus,
    localUpdatedAtEpochMillis = localUpdatedAtEpochMillis,
    lastSyncError = lastSyncError
)
