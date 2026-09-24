package com.example.pocketpilot.feature.analytics.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * User-facing choice for the analytics window. Presets resolve into concrete
 * [DateRange]s against a supplied clock; [Custom] carries the exact user-picked
 * dates so the filter survives configuration changes and process death.
 */
@Immutable
sealed interface DateRangeFilter {
    fun resolve(nowEpochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): DateRange

    fun label(): String

    data object ThisMonth : DateRangeFilter {
        override fun resolve(nowEpochMillis: Long, zone: ZoneId): DateRange = DateRange.containingMonth(nowEpochMillis, zone)

        override fun label(): String = "This month"
    }

    data object LastMonth : DateRangeFilter {
        override fun resolve(nowEpochMillis: Long, zone: ZoneId): DateRange {
            val current = DateRange.containingMonth(nowEpochMillis, zone)
            val previous = YearMonth.from(
                java.time.Instant.ofEpochMilli(current.fromEpochMillis).atZone(zone)
            ).minusMonths(1)
            return DateRange.ofMonth(previous, zone)
        }

        override fun label(): String = "Last month"
    }

    data object Last3Months : DateRangeFilter {
        override fun resolve(nowEpochMillis: Long, zone: ZoneId): DateRange {
            val current = YearMonth.from(
                java.time.Instant.ofEpochMilli(nowEpochMillis).atZone(zone)
            )
            val start = current.minusMonths(2)
            val startMillis = start.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val endMillis = current.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            return DateRange(startMillis, endMillis)
        }

        override fun label(): String = "Last 3 months"
    }

    data object ThisYear : DateRangeFilter {
        override fun resolve(nowEpochMillis: Long, zone: ZoneId): DateRange {
            val year = java.time.Instant.ofEpochMilli(nowEpochMillis).atZone(zone).year
            return DateRange.ofYear(year, zone)
        }

        override fun label(): String = "This year"
    }

    data object LastYear : DateRangeFilter {
        override fun resolve(nowEpochMillis: Long, zone: ZoneId): DateRange {
            val year = java.time.Instant.ofEpochMilli(nowEpochMillis).atZone(zone).year
            return DateRange.ofYear(year - 1, zone)
        }

        override fun label(): String = "Last year"
    }

    @Immutable
    data class Custom(val startInclusive: LocalDate, val endInclusive: LocalDate) : DateRangeFilter {
        init {
            require(!endInclusive.isBefore(startInclusive)) {
                "Custom range end must not precede start (start=$startInclusive, end=$endInclusive)"
            }
        }

        override fun resolve(nowEpochMillis: Long, zone: ZoneId): DateRange = DateRange.between(startInclusive, endInclusive, zone)

        override fun label(): String = "$startInclusive → $endInclusive"
    }

    companion object {
        val Presets: List<DateRangeFilter> = listOf(
            ThisMonth,
            LastMonth,
            Last3Months,
            ThisYear,
            LastYear
        )
    }
}
