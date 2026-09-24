package com.example.pocketpilot.feature.finance.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionValidatorTest {

    @Test
    fun `validateTitle rejects blank title`() {
        assertEquals(
            TransactionFieldError.TitleRequired,
            TransactionValidator.validateTitle("   ")
        )
    }

    @Test
    fun `validateTitle rejects titles over the max length`() {
        val overLong = "x".repeat(81)
        val error = TransactionValidator.validateTitle(overLong)
        assertTrue(error is TransactionFieldError.TitleTooLong)
    }

    @Test
    fun `validateTitle accepts a well-formed title`() {
        assertNull(TransactionValidator.validateTitle("Coffee at Starbucks"))
    }

    @Test
    fun `validateAmount rejects blank`() {
        assertEquals(TransactionFieldError.AmountRequired, TransactionValidator.validateAmount(" "))
    }

    @Test
    fun `validateAmount rejects non-numeric`() {
        assertEquals(TransactionFieldError.AmountInvalid, TransactionValidator.validateAmount("abc"))
    }

    @Test
    fun `validateAmount rejects zero`() {
        assertEquals(TransactionFieldError.AmountNonPositive, TransactionValidator.validateAmount("0"))
    }

    @Test
    fun `validateAmount rejects amounts that are too large`() {
        assertEquals(
            TransactionFieldError.AmountTooLarge,
            TransactionValidator.validateAmount("1000000000")
        )
    }

    @Test
    fun `validateAmount accepts comma decimal separator`() {
        assertNull(TransactionValidator.validateAmount("12,50"))
    }

    @Test
    fun `validateNote allows null`() {
        assertNull(TransactionValidator.validateNote(null))
    }

    @Test
    fun `validateNote rejects notes over the max length`() {
        val overLong = "n".repeat(241)
        val error = TransactionValidator.validateNote(overLong)
        assertTrue(error is TransactionFieldError.NoteTooLong)
    }
}
