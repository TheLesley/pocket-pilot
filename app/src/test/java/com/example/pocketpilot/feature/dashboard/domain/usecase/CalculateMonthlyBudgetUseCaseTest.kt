package com.example.pocketpilot.feature.dashboard.domain.usecase

import com.example.pocketpilot.feature.dashboard.domain.model.MonthRange
import com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod
import com.example.pocketpilot.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateMonthlyBudgetUseCaseTest {

    private val useCase = CalculateMonthlyBudgetUseCase()

    // A 30-day "month" for arithmetic clarity.
    private val monthMillis = 30L * 24L * 60L * 60L * 1_000L
    private val range = MonthRange(fromEpochMillis = 0L, toEpochMillis = monthMillis)

    @Test
    fun `empty budgets return zero`() {
        assertEquals(0L, useCase(emptyList(), range))
    }

    @Test
    fun `monthly budget contributes its limit as-is`() {
        val budget = Fixtures.budget(
            period = BudgetPeriod.MONTHLY,
            limitMinorUnits = 25_000L,
            startsAtEpochMillis = 0L,
            endsAtEpochMillis = null
        )
        assertEquals(25_000L, useCase(listOf(budget), range))
    }

    @Test
    fun `yearly budget is divided by twelve`() {
        val budget = Fixtures.budget(
            period = BudgetPeriod.YEARLY,
            limitMinorUnits = 120_000L,
            startsAtEpochMillis = 0L,
            endsAtEpochMillis = null
        )
        assertEquals(10_000L, useCase(listOf(budget), range))
    }

    @Test
    fun `budgets that do not overlap the month are excluded`() {
        val budget = Fixtures.budget(
            period = BudgetPeriod.MONTHLY,
            limitMinorUnits = 100_000L,
            startsAtEpochMillis = monthMillis, // starts exactly at end - not before to
            endsAtEpochMillis = monthMillis * 2
        )
        assertEquals(0L, useCase(listOf(budget), range))
    }
}
