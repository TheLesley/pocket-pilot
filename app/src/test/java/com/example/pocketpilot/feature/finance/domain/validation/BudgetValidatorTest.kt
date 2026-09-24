package com.example.pocketpilot.feature.finance.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BudgetValidatorTest {

    @Test
    fun `validateName rejects blank`() {
        assertEquals(BudgetFieldError.NameRequired, BudgetValidator.validateName(""))
    }

    @Test
    fun `validateLimit rejects zero`() {
        assertEquals(BudgetFieldError.LimitNonPositive, BudgetValidator.validateLimit("0"))
    }

    @Test
    fun `validateLimit accepts a well-formed positive number`() {
        assertNull(BudgetValidator.validateLimit("125.50"))
    }

    @Test
    fun `validateDateRange rejects end before start`() {
        assertEquals(
            BudgetFieldError.EndBeforeStart,
            BudgetValidator.validateDateRange(startsAt = 100L, endsAt = 50L)
        )
    }

    @Test
    fun `validateDateRange accepts null end (open-ended budget)`() {
        assertNull(BudgetValidator.validateDateRange(startsAt = 100L, endsAt = null))
    }

    @Test
    fun `validateCategory allows null`() {
        assertNull(BudgetValidator.validateCategory(null))
    }
}
