package com.example.pocketpilot.feature.analytics.domain.model

import androidx.compose.runtime.Immutable
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * Half-open epoch-millis window `[fromEpochMillis, toEpochMillis)` used by every
 * analytics use case. Kept independent of the dashboard's `MonthRange` because
 * analytics needs to represent arbitrary spans (quarters, custom ranges) not
 * just a single calendar month.
 */
@Immutable
data class DateRange(val fromEpochMillis: Long, val toEpochMillis: Long) {
    init {
        require(toEpochMillis >= fromEpochMillis) {
            "DateRange end must not precede start (from=$fromEpochMillis, to=$toEpochMillis)"
        }
    }

    operator fun contains(epochMillis: Long): Boolean = epochMillis in fromEpochMillis until toEpochMillis

    val durationMillis: Long get() = toEpochMillis - fromEpochMillis

    companion object {
        fun ofMonth(yearMonth: YearMonth, zone: ZoneId = ZoneId.systemDefault()): DateRange {
            val start = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val end = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            return DateRange(start, end)
        }

        fun ofYear(year: Int, zone: ZoneId = ZoneId.systemDefault()): DateRange {
            val start = LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli()
            val end = LocalDate.of(year + 1, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli()
            return DateRange(start, end)
        }

        fun between(startInclusive: LocalDate, endInclusive: LocalDate, zone: ZoneId = ZoneId.systemDefault()): DateRange {
            val start = startInclusive.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = endInclusive.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            return DateRange(start, end)
        }

        fun containingMonth(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): DateRange =
            ofMonth(YearMonth.from(Instant.ofEpochMilli(epochMillis).atZone(zone)), zone)
    }
}
