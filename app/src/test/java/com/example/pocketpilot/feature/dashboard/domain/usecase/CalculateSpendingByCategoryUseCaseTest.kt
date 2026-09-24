package com.example.pocketpilot.feature.dashboard.domain.usecase

import com.example.pocketpilot.feature.dashboard.domain.model.MonthRange
import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateSpendingByCategoryUseCaseTest {

    private val useCase = CalculateSpendingByCategoryUseCase()
    private val range = MonthRange(fromEpochMillis = 0L, toEpochMillis = 10_000L)

    @Test
    fun `groups by category, sorts largest first, and computes share`() {
        val transactions = listOf(
            Fixtures.transaction(
                id = "1",
                categoryId = "food",
                amountMinorUnits = 3_000L,
                occurredAtEpochMillis = 1_000L
            ),
            Fixtures.transaction(
                id = "2",
                categoryId = "transport",
                amountMinorUnits = 1_000L,
                occurredAtEpochMillis = 2_000L
            ),
            Fixtures.transaction(
                id = "3",
                categoryId = "food",
                amountMinorUnits = 2_000L,
                occurredAtEpochMillis = 3_000L
            )
        )

        val result = useCase(transactions, range)

        assertEquals(2, result.size)
        assertEquals("food", result[0].categoryId)
        assertEquals(5_000L, result[0].amountMinorUnits)
        // food = 5000 / 6000 ≈ 0.833
        assertTrue(result[0].share > 0.83f && result[0].share < 0.84f)
        assertEquals("transport", result[1].categoryId)
    }

    @Test
    fun `uncategorised expenses collapse into a single bucket labelled Uncategorised`() {
        val transactions = listOf(
            Fixtures.transaction(id = "1", categoryId = null, amountMinorUnits = 700L, occurredAtEpochMillis = 1_000L),
            Fixtures.transaction(id = "2", categoryId = "  ", amountMinorUnits = 300L, occurredAtEpochMillis = 2_000L)
        )

        val result = useCase(transactions, range)

        assertEquals(1, result.size)
        assertEquals(null, result[0].categoryId)
        assertEquals("Uncategorised", result[0].label)
        assertEquals(1_000L, result[0].amountMinorUnits)
    }

    @Test
    fun `income is excluded`() {
        val transactions = listOf(
            Fixtures.transaction(
                id = "1",
                type = TransactionType.INCOME,
                categoryId = "salary",
                amountMinorUnits = 100_000L,
                occurredAtEpochMillis = 1_000L
            )
        )

        assertEquals(emptyList<Any>(), useCase(transactions, range))
    }
}
