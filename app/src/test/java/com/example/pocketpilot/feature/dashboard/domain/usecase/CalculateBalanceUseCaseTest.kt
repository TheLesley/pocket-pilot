package com.example.pocketpilot.feature.dashboard.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateBalanceUseCaseTest {

    private val useCase = CalculateBalanceUseCase()

    @Test
    fun `empty list yields zero balance`() {
        assertEquals(0L, useCase(emptyList()))
    }

    @Test
    fun `income adds, expense subtracts`() {
        val transactions = listOf(
            Fixtures.transaction(id = "1", type = TransactionType.INCOME, amountMinorUnits = 10_000L),
            Fixtures.transaction(id = "2", type = TransactionType.EXPENSE, amountMinorUnits = 3_000L),
            Fixtures.transaction(id = "3", type = TransactionType.EXPENSE, amountMinorUnits = 2_500L)
        )

        assertEquals(4_500L, useCase(transactions))
    }

    @Test
    fun `expenses exceeding income yield negative balance`() {
        val transactions = listOf(
            Fixtures.transaction(id = "1", type = TransactionType.INCOME, amountMinorUnits = 1_000L),
            Fixtures.transaction(id = "2", type = TransactionType.EXPENSE, amountMinorUnits = 5_000L)
        )

        assertEquals(-4_000L, useCase(transactions))
    }
}
