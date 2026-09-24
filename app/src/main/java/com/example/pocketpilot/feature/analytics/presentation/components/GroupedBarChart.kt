package com.example.pocketpilot.feature.analytics.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Bar-group descriptor for [GroupedBarChart]. Every group in a chart must have
 * the same number of bars — the chart lays them out with a fixed gap so
 * grouped comparisons (income vs. expense) render evenly.
 */
data class BarGroup(val label: String, val bars: List<BarValue>) {
    data class BarValue(val value: Float, val color: Color)
}

/**
 * Grouped bar chart used for the "income vs. expense" comparison. Draws bars
 * with rounded caps via a horizontal cap rectangle so we stay Canvas-only and
 * avoid Path allocations per frame.
 */
@Composable
fun GroupedBarChart(
    groups: List<BarGroup>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 200.dp,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val maxValue = groups.flatMap { it.bars.map { b -> b.value } }.maxOrNull()?.takeIf { it > 0f } ?: 1f

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val topPadding = 8f
            val bottomPadding = 8f
            val sidePadding = 4f
            val plotWidth = size.width - 2 * sidePadding
            val plotHeight = size.height - topPadding - bottomPadding

            // Horizontal grid.
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = topPadding + plotHeight * i / gridLines
                drawLine(
                    color = gridColor.copy(alpha = 0.6f),
                    start = Offset(sidePadding, y),
                    end = Offset(sidePadding + plotWidth, y),
                    strokeWidth = 1f
                )
            }

            if (groups.isEmpty()) return@Canvas

            val groupSlot = plotWidth / groups.size
            val innerBars = groups.first().bars.size.coerceAtLeast(1)
            val betweenGroupPadding = groupSlot * 0.2f
            val barsWidth = groupSlot - betweenGroupPadding
            val barGap = 4f
            val singleBarWidth = ((barsWidth - barGap * (innerBars - 1)) / innerBars).coerceAtLeast(2f)

            groups.forEachIndexed { groupIndex, group ->
                val groupLeft = sidePadding + groupSlot * groupIndex + betweenGroupPadding / 2f
                group.bars.forEachIndexed { barIndex, bar ->
                    val normalized = (bar.value / maxValue).coerceIn(0f, 1f)
                    val barHeight = plotHeight * normalized
                    val x = groupLeft + (singleBarWidth + barGap) * barIndex
                    val y = topPadding + (plotHeight - barHeight)
                    drawRect(
                        color = bar.color,
                        topLeft = Offset(x, y),
                        size = Size(singleBarWidth, barHeight)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            groups.forEach { group ->
                Text(
                    text = group.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
