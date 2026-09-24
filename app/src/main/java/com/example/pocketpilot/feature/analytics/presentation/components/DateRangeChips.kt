package com.example.pocketpilot.feature.analytics.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pocketpilot.feature.analytics.domain.model.DateRangeFilter

/**
 * Horizontally-scrolling filter-chip row for the analytics date window. A
 * trailing "Custom" chip is always visible so users can open the date picker
 * even when the preset list is long.
 */
@Composable
fun DateRangeChips(
    selected: DateRangeFilter,
    presets: List<DateRangeFilter>,
    onPresetSelected: (DateRangeFilter) -> Unit,
    onCustomRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onPresetSelected(filter) },
                label = { Text(text = filter.label()) }
            )
        }
        val customSelected = selected is DateRangeFilter.Custom
        FilterChip(
            selected = customSelected,
            onClick = onCustomRequested,
            label = {
                Text(
                    text = if (customSelected) (selected as DateRangeFilter.Custom).label() else "Custom"
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier
                        .then(Modifier)
                )
            },
            colors = FilterChipDefaults.filterChipColors()
        )
    }
}
