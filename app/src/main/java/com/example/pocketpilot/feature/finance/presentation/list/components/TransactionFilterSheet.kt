package com.example.pocketpilot.feature.finance.presentation.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.feature.finance.domain.model.SortDirection
import com.example.pocketpilot.feature.finance.domain.model.TransactionSortField
import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.feature.finance.presentation.list.TransactionListEvent
import com.example.pocketpilot.feature.finance.presentation.list.TransactionListState
import com.example.pocketpilot.feature.finance.presentation.util.DateFormatter

/**
 * Modal filter sheet. Every control is bound directly to the ViewModel via the
 * `onEvent` callback so the sheet is a pure projection of state — no local
 * copies drift, no "apply" button is needed, and the trailing "Clear all"
 * empties every facet at once.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterSheet(state: TransactionListState, onEvent: (TransactionListEvent) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(TransactionListEvent.DismissFilterSheet) },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = PocketPilotTheme.spacing.lg,
                    vertical = PocketPilotTheme.spacing.md
                ),
            verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.lg)
        ) {
            Text(
                text = "Filter transactions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            FilterSection(title = "Type") {
                Row(horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)) {
                    TransactionType.values().forEach { type ->
                        FilterChip(
                            selected = type in state.typeFilters,
                            onClick = { onEvent(TransactionListEvent.TypeFilterToggled(type)) },
                            label = {
                                Text(
                                    text = when (type) {
                                        TransactionType.INCOME -> "Income"
                                        TransactionType.EXPENSE -> "Expense"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            if (state.availableCategoryIds.isNotEmpty()) {
                FilterSection(title = "Categories") {
                    FlowChipRow {
                        state.availableCategoryIds.forEach { categoryId ->
                            FilterChip(
                                selected = categoryId in state.categoryFilters,
                                onClick = {
                                    onEvent(TransactionListEvent.CategoryFilterToggled(categoryId))
                                },
                                label = { Text(categoryId) }
                            )
                        }
                    }
                }
            }

            DateRangeSection(state = state, onEvent = onEvent)

            AmountRangeSection(state = state, onEvent = onEvent)

            FilterSection(title = "Sort by") {
                FlowChipRow {
                    TransactionSortField.values().forEach { field ->
                        val selected = field == state.sortField
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val nextDirection = if (selected) {
                                    state.sortDirection.toggle()
                                } else {
                                    state.sortDirection
                                }
                                onEvent(TransactionListEvent.SortChanged(field, nextDirection))
                            },
                            label = {
                                val suffix = if (selected) " ${state.sortDirection.arrow()}" else ""
                                Text("${field.displayName()}$suffix")
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.md)
            ) {
                OutlinedButton(
                    onClick = { onEvent(TransactionListEvent.ClearFilters) },
                    modifier = Modifier.weight(1f),
                    enabled = state.hasActiveFilters
                ) { Text("Clear all") }
                Button(
                    onClick = { onEvent(TransactionListEvent.DismissFilterSheet) },
                    modifier = Modifier.weight(1f)
                ) { Text("Done") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeSection(state: TransactionListState, onEvent: (TransactionListEvent) -> Unit) {
    var pickerVisible by remember { mutableStateOf(false) }
    FilterSection(title = "Date range") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
        ) {
            OutlinedButton(
                onClick = { pickerVisible = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when {
                        state.fromDateMillis == null && state.toDateMillis == null -> "Pick dates"
                        else -> {
                            val from = state.fromDateMillis?.let { DateFormatter.formatShort(it) } ?: "…"
                            val to = state.toDateMillis?.let { DateFormatter.formatShort(it) } ?: "…"
                            "$from → $to"
                        }
                    }
                )
            }
            if (state.fromDateMillis != null || state.toDateMillis != null) {
                OutlinedButton(onClick = {
                    onEvent(TransactionListEvent.DateRangeChanged(fromMillis = null, toMillis = null))
                }) { Text("Clear") }
            }
        }
    }
    if (pickerVisible) {
        com.example.pocketpilot.feature.analytics.presentation.components.CustomRangeDialog(
            initialStart = state.fromDateMillis?.let { DateFormatter.toLocalDate(it) },
            initialEnd = state.toDateMillis?.let { DateFormatter.toLocalDate(it) },
            onDismiss = { pickerVisible = false },
            onConfirm = { start, end ->
                pickerVisible = false
                onEvent(
                    TransactionListEvent.DateRangeChanged(
                        fromMillis = DateFormatter.startOfDay(start),
                        toMillis = DateFormatter.endOfDay(end)
                    )
                )
            }
        )
    }
}

@Composable
private fun AmountRangeSection(state: TransactionListState, onEvent: (TransactionListEvent) -> Unit) {
    val minText = remember(state.minAmountMinorUnits) {
        state.minAmountMinorUnits?.let { minorToMajor(it) }.orEmpty()
    }
    val maxText = remember(state.maxAmountMinorUnits) {
        state.maxAmountMinorUnits?.let { minorToMajor(it) }.orEmpty()
    }

    FilterSection(title = "Amount range") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
        ) {
            OutlinedTextField(
                value = minText,
                onValueChange = { raw ->
                    onEvent(
                        TransactionListEvent.AmountRangeChanged(
                            minMinorUnits = parseMajorToMinor(raw),
                            maxMinorUnits = state.maxAmountMinorUnits
                        )
                    )
                },
                label = { Text("Min") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxText,
                onValueChange = { raw ->
                    onEvent(
                        TransactionListEvent.AmountRangeChanged(
                            minMinorUnits = state.minAmountMinorUnits,
                            maxMinorUnits = parseMajorToMinor(raw)
                        )
                    )
                },
                label = { Text("Max") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

/**
 * Simple flowing chip row that wraps to a second line when it runs out of
 * horizontal space. Uses Material 3's FlowRow so long category lists stay
 * readable without horizontal scrolling.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowChipRow(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
    ) {
        content()
    }
}

private fun SortDirection.toggle(): SortDirection = if (this == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC

private fun SortDirection.arrow(): String = if (this == SortDirection.ASC) "↑" else "↓"

private fun TransactionSortField.displayName(): String = when (this) {
    TransactionSortField.DATE -> "Date"
    TransactionSortField.AMOUNT -> "Amount"
    TransactionSortField.TITLE -> "Title"
}

private fun minorToMajor(minor: Long): String = "%.2f".format(minor / 100.0)

private fun parseMajorToMinor(raw: String): Long? {
    if (raw.isBlank()) return null
    val normalized = raw.replace(",", ".")
    val parsed = normalized.toDoubleOrNull() ?: return null
    if (parsed < 0.0) return null
    return Math.round(parsed * 100.0)
}
