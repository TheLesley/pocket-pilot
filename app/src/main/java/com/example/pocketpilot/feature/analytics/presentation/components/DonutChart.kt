package com.example.pocketpilot.feature.analytics.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Slice descriptor for [DonutChart]. [value] must be non-negative; the chart
 * normalises the values to sum to 1.0 internally so the caller does not have
 * to pre-compute percentages.
 */
data class DonutSlice(val label: String, val value: Float, val color: Color, val amountText: String)

/**
 * Compact donut chart drawn purely with `Canvas` primitives so we avoid pulling
 * in a third-party charting library. The stroke width scales with the canvas
 * so the chart looks sensible from ~120.dp up to full-width layouts.
 *
 * A center label is optional and typically used to show the total value.
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    centerLabel: String? = null,
    centerValue: String? = null,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            val minDim = size.minDimension
            val strokeWidth = minDim * STROKE_FRACTION
            val topLeft = Offset(
                (size.width - minDim) / 2f + strokeWidth / 2f,
                (size.height - minDim) / 2f + strokeWidth / 2f
            )
            val arcSize = Size(minDim - strokeWidth, minDim - strokeWidth)

            // Track keeps the donut visible even when every value is zero.
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            if (total <= 0f) return@Canvas

            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total) * 360f
                if (sweep > 0f) {
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweep
                }
            }
        }

        if (centerLabel != null || centerValue != null) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (centerValue != null) {
                    Text(
                        text = centerValue,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (centerLabel != null) {
                    Text(
                        text = centerLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DonutLegendItem(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                drawCircle(color = color)
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private const val STROKE_FRACTION = 0.18f
