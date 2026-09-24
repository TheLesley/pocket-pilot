package com.example.pocketpilot.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme

/**
 * Reusable horizontal progress bar for budget usage. Colour is derived from
 * the ratio so callers can pass a raw `spent / limit` value and the component
 * conveys `ON_TRACK`, `WARNING` (≥80%), and `EXCEEDED` (≥100%) states.
 *
 * The bar clamps visually to `[0f, 1f]` even for overspend values so the fill
 * never runs off the edge; a text label alongside is expected to communicate
 * the exact percentage.
 */
@Composable
fun BudgetProgressIndicator(
    ratio: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onTrackColor: Color = MaterialTheme.colorScheme.primary,
    warningColor: Color = MaterialTheme.colorScheme.tertiary,
    exceededColor: Color = MaterialTheme.colorScheme.error
) {
    val clamped = ratio.coerceIn(0f, 1f)
    val fillColor = when {
        ratio >= 1f -> exceededColor
        ratio >= 0.8f -> warningColor
        else -> onTrackColor
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(MaterialTheme.shapes.small)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clamped)
                .background(fillColor)
        )
    }
}

@Preview
@Composable
private fun BudgetProgressIndicatorOnTrackPreview() {
    PocketPilotTheme {
        BudgetProgressIndicator(ratio = 0.4f)
    }
}

@Preview
@Composable
private fun BudgetProgressIndicatorWarningPreview() {
    PocketPilotTheme {
        BudgetProgressIndicator(ratio = 0.85f)
    }
}

@Preview
@Composable
private fun BudgetProgressIndicatorExceededPreview() {
    PocketPilotTheme {
        BudgetProgressIndicator(ratio = 1.2f)
    }
}
