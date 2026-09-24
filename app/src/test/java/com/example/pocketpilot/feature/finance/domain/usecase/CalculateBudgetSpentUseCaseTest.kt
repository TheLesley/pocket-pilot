package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateBudgetSpentUseCaseTest {

    private val useCase = CalculateBudgetSpentUseCase()

    @Test
    fun `sums expenses linked directly via budgetId`() {
        val budget = Fixtures.budget(
            id = "b1",
            startsAtEpochMillis = 0L,
            endsAtEpochMillis = 10_000L,
            categoryId = null
        )
        val transactions = listOf(
            Fixtures.transaction(id = "1", budgetId = "b1", amountMinorUnits = 400L, occurredAtEpochMillis = 100L),
            Fixtures.transaction(id = "2", budgetId = "b1", amountMinorUnits = 600L, occurredAtEpochMillis = 200L),
            Fixtures.transaction(id = "3", budgetId = "other", amountMinorUnits = 999L, occurredAtEpochMillis = 300L)
        )

        assertEquals(1_000L, useCase(budget, transactions))
    }

    @Test
    fun `matches by categoryId when budget is category-scoped`() {
        val budget = Fixtures.budget(
            id = "b1",
            startsAtEpochMillis = 0L,
            endsAtEpochMillis = null,
            categoryId = "food"
        )
        val transactions = listOf(
            Fixtures.transaction(id = "1", categoryId = "food", budgetId = null, amountMinorUnits = 250L),
            Fixtures.transaction(id = "2", categoryId = "food", budgetId = null, amountMinorUnits = 750L),
            Fixtures.transaction(id = "3", categoryId = "transport", budgetId = null, amountMinorUnits = 900L)
        )

        assertEquals(1_000L, useCase(budget, transactions))
    }

    @Test
    fun `excludes income, mismatched currency, and out-of-window transactions`() {
        val budget = Fixtures.budget(
            id = "b1",
            startsAtEpochMillis = 100L,
            endsAtEpochMillis = 200L,
            currencyCode = "USD",
            categoryId = "food"
        )
        val transactions = listOf(
            // income - excluded
            Fixtures.transaction(
                id = "in",
                type = TransactionType.INCOME,
                categoryId = "food",
                amountMinorUnits = 999L,
                occurredAtEpochMillis = 150L
            ),
            // wrong currency
            Fixtures.transaction(
                id = "eur",
                currencyCode = "EUR",
                categoryId = "food",
                amountMinorUnits = 999L,
                occurredAtEpochMillis = 150L
            ),
            // before start
            Fixtures.transaction(id = "old", categoryId = "food", amountMinorUnits = 999L, occurredAtEpochMillis = 50L),
            // after end
            Fixtures.transaction(id = "new", categoryId = "food", amountMinorUnits = 999L, occurredAtEpochMillis = 250L),
            // valid
            Fixtures.transaction(id = "ok", categoryId = "food", amountMinorUnits = 300L, occurredAtEpochMillis = 150L)
        )

        assertEquals(300L, useCase(budget, transactions))
    }
}
