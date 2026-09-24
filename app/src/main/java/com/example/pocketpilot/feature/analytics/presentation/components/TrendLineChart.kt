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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Series descriptor for the trend line chart. The `values` list must be the
 * same length as the labels passed to [TrendLineChart].
 */
data class LineSeries(val label: String, val values: List<Float>, val color: Color)

/**
 * Multi-series line chart with a light dotted baseline and grid lines. Kept
 * intentionally small — no interactive tooltips or animated draw-in — so it
 * behaves predictably on lower-end devices and inside scrollable columns.
 */
@Composable
fun TrendLineChart(
    labels: List<String>,
    series: List<LineSeries>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 180.dp,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val effectiveSeries = series.filter { it.values.size == labels.size }
    val maxValue = effectiveSeries.flatMap { it.values }.maxOrNull()?.takeIf { it > 0f } ?: 1f

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val topPadding = 8f
            val bottomPadding = 8f
            val leftPadding = 4f
            val rightPadding = 4f
            val plotWidth = size.width - leftPadding - rightPadding
            val plotHeight = size.height - topPadding - bottomPadding

            // Baseline grid (four evenly spaced horizontal lines).
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = topPadding + plotHeight * i / gridLines
                drawLine(
                    color = gridColor.copy(alpha = 0.6f),
                    start = Offset(leftPadding, y),
                    end = Offset(leftPadding + plotWidth, y),
                    strokeWidth = 1f
                )
            }

            if (labels.isEmpty()) return@Canvas

            val stepX = if (labels.size > 1) plotWidth / (labels.size - 1) else 0f

            effectiveSeries.forEach { line ->
                val path = Path()
                line.values.forEachIndexed { index, value ->
                    val x = leftPadding + stepX * index +
                        if (labels.size == 1) plotWidth / 2f else 0f
                    val normalized = (value / maxValue).coerceIn(0f, 1f)
                    val y = topPadding + plotHeight * (1f - normalized)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    drawCircle(color = line.color, radius = 4f, center = Offset(x, y))
                }
                drawPath(
                    path = path,
                    color = line.color,
                    style = Stroke(width = 4f)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
