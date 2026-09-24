package com.example.pocketpilot.feature.finance.presentation.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {

    private val fullFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

    private val shortFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

    fun formatFull(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(fullFormatter)

    fun formatShort(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(shortFormatter)

    fun toLocalDate(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    fun startOfDay(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun endOfDay(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
}
