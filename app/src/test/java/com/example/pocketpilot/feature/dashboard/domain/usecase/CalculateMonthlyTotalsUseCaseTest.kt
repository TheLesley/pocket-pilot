package com.example.pocketpilot.feature.dashboard.domain.usecase

import com.example.pocketpilot.feature.dashboard.domain.model.MonthRange
import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateMonthlyTotalsUseCaseTest {

    private val useCase = CalculateMonthlyTotalsUseCase()
    private val range = MonthRange(fromEpochMillis = 1_000L, toEpochMillis = 2_000L)

    @Test
    fun `sums income and expenses inside range`() {
        val transactions = listOf(
            Fixtures.transaction(
                id = "in",
                type = TransactionType.INCOME,
                amountMinorUnits = 5_000L,
                occurredAtEpochMillis = 1_500L
            ),
            Fixtures.transaction(
                id = "out",
                type = TransactionType.EXPENSE,
                amountMinorUnits = 1_200L,
                occurredAtEpochMillis = 1_800L
            )
        )

        val totals = useCase(transactions, range)

        assertEquals(5_000L, totals.incomeMinorUnits)
        assertEquals(1_200L, totals.expenseMinorUnits)
    }

    @Test
    fun `excludes transactions outside range`() {
        val transactions = listOf(
            Fixtures.transaction(
                id = "before",
                type = TransactionType.INCOME,
                amountMinorUnits = 9_999L,
                occurredAtEpochMillis = 999L
            ),
            Fixtures.transaction(
                id = "after",
                type = TransactionType.EXPENSE,
                amountMinorUnits = 8_888L,
                occurredAtEpochMillis = 2_000L // upper bound is exclusive
            )
        )

        val totals = useCase(transactions, range)

        assertEquals(0L, totals.incomeMinorUnits)
        assertEquals(0L, totals.expenseMinorUnits)
    }
}
