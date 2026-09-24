package com.example.pocketpilot.feature.analytics.presentation.components

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Wraps [DateRangePicker] in a [DatePickerDialog] so callers only receive
 * `LocalDate` values — the analytics domain never leaks `epochMillis` to the
 * ViewModel or the UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRangeDialog(initialStart: LocalDate?, initialEnd: LocalDate?, onDismiss: () -> Unit, onConfirm: (LocalDate, LocalDate) -> Unit) {
    val startMillis = remember(initialStart) { initialStart?.toUtcMillis() }
    val endMillis = remember(initialEnd) { initialEnd?.toUtcMillis() }
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startMillis,
        initialSelectedEndDateMillis = endMillis,
        initialDisplayMode = DisplayMode.Picker
    )
    val confirmEnabled by remember {
        derivedStateOf {
            pickerState.selectedStartDateMillis != null && pickerState.selectedEndDateMillis != null
        }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = confirmEnabled,
                onClick = {
                    val start = pickerState.selectedStartDateMillis?.toLocalDate() ?: return@TextButton
                    val end = pickerState.selectedEndDateMillis?.toLocalDate() ?: return@TextButton
                    onConfirm(start, end)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        colors = DatePickerDefaults.colors()
    ) {
        DateRangePicker(
            state = pickerState,
            showModeToggle = false,
            title = { Text("Select date range") }
        )
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).toLocalDate()
