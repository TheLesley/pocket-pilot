package com.example.pocketpilot.feature.finance.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class MoneyFormatterTest {

    @Test
    fun `formats minor units using the currency's fraction digits`() {
        val formatted = MoneyFormatter.format(
            minorUnits = 12_345L,
            currencyCode = "USD",
            locale = Locale.US
        )
        assertEquals("$123.45", formatted)
    }

    @Test
    fun `parseToMinorUnits rejects non-numeric input`() {
        assertNull(MoneyFormatter.parseToMinorUnits("not-a-number", "USD"))
    }

    @Test
    fun `parseToMinorUnits accepts comma decimal separator and rounds correctly`() {
        assertEquals(12_345L, MoneyFormatter.parseToMinorUnits("123,45", "USD"))
    }

    @Test
    fun `parseToMinorUnits rejects negative amounts`() {
        assertNull(MoneyFormatter.parseToMinorUnits("-5", "USD"))
    }

    @Test
    fun `parseToMinorUnits falls back to two fraction digits for unknown currency`() {
        assertEquals(100L, MoneyFormatter.parseToMinorUnits("1.00", "ZZZ"))
    }
}
