package com.example.pocketpilot.testutil

import com.example.pocketpilot.feature.finance.domain.model.Budget
import com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

object Fixtures {

    fun transaction(
        id: String = "tx-1",
        title: String = "Coffee",
        amountMinorUnits: Long = 500L,
        currencyCode: String = "USD",
        type: TransactionType = TransactionType.EXPENSE,
        categoryId: String? = "coffee",
        budgetId: String? = null,
        occurredAtEpochMillis: Long = 1_700_000_000_000L,
        note: String? = null,
        createdAtEpochMillis: Long = occurredAtEpochMillis,
        updatedAtEpochMillis: Long = occurredAtEpochMillis
    ): Transaction = Transaction(
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

    fun budget(
        id: String = "budget-1",
        name: String = "Coffee budget",
        limitMinorUnits: Long = 10_000L,
        currencyCode: String = "USD",
        period: BudgetPeriod = BudgetPeriod.MONTHLY,
        startsAtEpochMillis: Long = 0L,
        endsAtEpochMillis: Long? = null,
        categoryId: String? = "coffee",
        createdAtEpochMillis: Long = 0L,
        updatedAtEpochMillis: Long = 0L
    ): Budget = Budget(
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

    fun savingsGoal(
        id: String = "goal-1",
        name: String = "Vacation",
        targetMinorUnits: Long = 500_000L,
        savedMinorUnits: Long = 100_000L,
        currencyCode: String = "USD",
        targetDateEpochMillis: Long? = null,
        note: String? = null,
        createdAtEpochMillis: Long = 0L,
        updatedAtEpochMillis: Long = 0L
    ): SavingsGoal = SavingsGoal(
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
}
