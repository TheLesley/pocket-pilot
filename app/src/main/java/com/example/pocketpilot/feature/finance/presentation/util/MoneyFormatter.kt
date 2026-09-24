package com.example.pocketpilot.feature.finance.presentation.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Formats a minor-unit amount + ISO currency code into a locale-aware display
 * string. Falls back to a plain "code amount" if the currency is unknown to
 * the JVM (unit tests may run with a currency the JVM doesn't recognise).
 */
object MoneyFormatter {

    fun format(minorUnits: Long, currencyCode: String, locale: Locale = Locale.getDefault()): String {
        val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
        val fractionDigits = currency?.defaultFractionDigits?.coerceAtLeast(0) ?: 2
        val divisor = pow10(fractionDigits)
        val majorUnits = minorUnits.toDouble() / divisor
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            if (currency != null) this.currency = currency
            minimumFractionDigits = fractionDigits
            maximumFractionDigits = fractionDigits
        }
        return runCatching { formatter.format(majorUnits) }
            .getOrElse { "$currencyCode ${"%,.${fractionDigits}f".format(majorUnits)}" }
    }

    fun parseToMinorUnits(raw: String, currencyCode: String): Long? {
        val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
        val fractionDigits = currency?.defaultFractionDigits?.coerceAtLeast(0) ?: 2
        val normalized = raw.trim().replace(",", ".")
        val parsed = normalized.toDoubleOrNull() ?: return null
        if (parsed < 0.0) return null
        return Math.round(parsed * pow10(fractionDigits))
    }

    private fun pow10(exp: Int): Double {
        var result = 1.0
        repeat(exp) { result *= 10.0 }
        return result
    }
}
