package com.example.pocketpilot.feature.dashboard.domain.model

import androidx.compose.runtime.Immutable
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/**
 * Half-open epoch-millis range `[fromEpochMillis, toEpochMillis)` describing
 * the calendar month that contains a reference instant. Used by dashboard use
 * cases to slice transactions into the "this month" bucket without leaking
 * `java.time` into the ViewModel or UI.
 */
@Immutable
data class MonthRange(val fromEpochMillis: Long, val toEpochMillis: Long) {
    operator fun contains(epochMillis: Long): Boolean = epochMillis in fromEpochMillis until toEpochMillis

    companion object {
        fun containing(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): MonthRange {
            val yearMonth = YearMonth.from(Instant.ofEpochMilli(epochMillis).atZone(zone))
            val start = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val end = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            return MonthRange(fromEpochMillis = start, toEpochMillis = end)
        }
    }
}
