package com.example.pocketpilot.feature.finance.presentation.list.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.feature.finance.presentation.list.TransactionListEvent
import com.example.pocketpilot.feature.finance.presentation.list.TransactionListState
import com.example.pocketpilot.feature.finance.presentation.util.DateFormatter

/**
 * Renders one dismissible chip per active facet: category, type, date range,
 * and amount range. Tapping the trailing "x" fires the exact event that clears
 * that facet — no confirmation, no batched "apply".
 */
@Composable
fun ActiveFilterChips(state: TransactionListState, onEvent: (TransactionListEvent) -> Unit, modifier: Modifier = Modifier) {
    if (!state.hasActiveFilters) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(PocketPilotTheme.spacing.sm)
    ) {
        state.typeFilters.forEach { type ->
            DismissibleChip(
                label = when (type) {
                    TransactionType.INCOME -> "Type: Income"
                    TransactionType.EXPENSE -> "Type: Expense"
                },
                onDismiss = { onEvent(TransactionListEvent.TypeFilterToggled(type)) }
            )
        }
        state.categoryFilters.forEach { categoryId ->
            DismissibleChip(
                label = "Category: $categoryId",
                onDismiss = { onEvent(TransactionListEvent.CategoryFilterToggled(categoryId)) }
            )
        }
        if (state.fromDateMillis != null || state.toDateMillis != null) {
            DismissibleChip(
                label = dateRangeLabel(state.fromDateMillis, state.toDateMillis),
                onDismiss = {
                    onEvent(TransactionListEvent.DateRangeChanged(fromMillis = null, toMillis = null))
                }
            )
        }
        if (state.minAmountMinorUnits != null || state.maxAmountMinorUnits != null) {
            DismissibleChip(
                label = amountRangeLabel(state.minAmountMinorUnits, state.maxAmountMinorUnits),
                onDismiss = {
                    onEvent(
                        TransactionListEvent.AmountRangeChanged(
                            minMinorUnits = null,
                            maxMinorUnits = null
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun DismissibleChip(label: String, onDismiss: () -> Unit) {
    AssistChip(
        onClick = onDismiss,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove filter",
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

private fun dateRangeLabel(fromMillis: Long?, toMillis: Long?): String {
    val from = fromMillis?.let { DateFormatter.formatShort(it) } ?: "Any"
    val to = toMillis?.let { DateFormatter.formatShort(it) } ?: "Any"
    return "$from → $to"
}

private fun amountRangeLabel(minMinor: Long?, maxMinor: Long?): String {
    val min = minMinor?.let { formatMinor(it) } ?: "Any"
    val max = maxMinor?.let { formatMinor(it) } ?: "Any"
    return "$min – $max"
}

private fun formatMinor(minor: Long): String {
    val major = minor / 100.0
    return "%,.2f".format(major)
}
