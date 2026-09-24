package com.example.pocketpilot.feature.analytics.domain.usecase

import com.example.pocketpilot.feature.analytics.domain.model.DateRange
import com.example.pocketpilot.feature.analytics.domain.model.TrendGranularity
import com.example.pocketpilot.feature.analytics.domain.model.TrendPoint
import com.example.pocketpilot.feature.analytics.domain.model.TrendSeries
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Slices [transactions] into evenly-spaced buckets across [range] and returns a
 * chronologically ordered [TrendSeries]. Empty buckets are preserved so the
 * chart x-axis stays uniformly spaced.
 *
 * [granularity] is picked automatically when null — a window longer than
 * ~18 months collapses to yearly buckets so the chart doesn't become an
 * unreadable forest of thin bars.
 */
class CalculateTrendSeriesUseCase(private val zone: ZoneId = ZoneId.systemDefault()) {

    operator fun invoke(transactions: List<Transaction>, range: DateRange, granularity: TrendGranularity? = null): TrendSeries {
        val resolved = granularity ?: pickGranularity(range)
        return when (resolved) {
            TrendGranularity.MONTHLY -> buildMonthly(transactions, range)
            TrendGranularity.YEARLY -> buildYearly(transactions, range)
        }
    }

    private fun pickGranularity(range: DateRange): TrendGranularity {
        val startYm = YearMonth.from(Instant.ofEpochMilli(range.fromEpochMillis).atZone(zone))
        val endInclusive = Instant.ofEpochMilli(range.toEpochMillis - 1).atZone(zone)
        val endYm = YearMonth.from(endInclusive)
        val months = ((endYm.year - startYm.year) * 12 + (endYm.monthValue - startYm.monthValue)) + 1
        return if (months > MONTHLY_LIMIT) TrendGranularity.YEARLY else TrendGranularity.MONTHLY
    }

    private fun buildMonthly(transactions: List<Transaction>, range: DateRange): TrendSeries {
        val startYm = YearMonth.from(Instant.ofEpochMilli(range.fromEpochMillis).atZone(zone))
        val endYmExclusive = YearMonth.from(
            Instant.ofEpochMilli(range.toEpochMillis - 1).atZone(zone)
        ).plusMonths(1)

        val buckets = LinkedHashMap<YearMonth, LongArray>()
        var cursor = startYm
        while (cursor.isBefore(endYmExclusive)) {
            buckets[cursor] = LongArray(2)
            cursor = cursor.plusMonths(1)
        }

        for (tx in transactions) {
            if (tx.occurredAtEpochMillis !in range) continue
            val ym = YearMonth.from(Instant.ofEpochMilli(tx.occurredAtEpochMillis).atZone(zone))
            val bucket = buckets[ym] ?: continue
            when (tx.type) {
                TransactionType.INCOME -> bucket[0] += tx.amountMinorUnits
                TransactionType.EXPENSE -> bucket[1] += tx.amountMinorUnits
            }
        }

        val points = buckets.entries.map { (ym, values) ->
            TrendPoint(
                key = ym.toString(),
                label = ym.format(MONTH_LABEL),
                incomeMinorUnits = values[0],
                expenseMinorUnits = values[1]
            )
        }
        return TrendSeries(
            granularity = TrendGranularity.MONTHLY,
            range = range,
            points = points
        )
    }

    private fun buildYearly(transactions: List<Transaction>, range: DateRange): TrendSeries {
        val startYear = Instant.ofEpochMilli(range.fromEpochMillis).atZone(zone).year
        val endYearInclusive = Instant.ofEpochMilli(range.toEpochMillis - 1).atZone(zone).year

        val buckets = LinkedHashMap<Int, LongArray>()
        for (year in startYear..endYearInclusive) {
            buckets[year] = LongArray(2)
        }

        for (tx in transactions) {
            if (tx.occurredAtEpochMillis !in range) continue
            val year = Instant.ofEpochMilli(tx.occurredAtEpochMillis).atZone(zone).year
            val bucket = buckets[year] ?: continue
            when (tx.type) {
                TransactionType.INCOME -> bucket[0] += tx.amountMinorUnits
                TransactionType.EXPENSE -> bucket[1] += tx.amountMinorUnits
            }
        }

        val points = buckets.entries.map { (year, values) ->
            TrendPoint(
                key = year.toString(),
                label = year.toString(),
                incomeMinorUnits = values[0],
                expenseMinorUnits = values[1]
            )
        }
        return TrendSeries(
            granularity = TrendGranularity.YEARLY,
            range = range,
            points = points
        )
    }

    private companion object {
        const val MONTHLY_LIMIT = 18

        // `LLL` gives the short standalone month name (e.g. "Jan"), which reads
        // correctly on a horizontal axis in every western locale.
        val MONTH_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("LLL", Locale.getDefault())
    }
}
