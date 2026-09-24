package com.example.pocketpilot.feature.analytics.presentation.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Deterministic palette shared by the pie / bar / line charts. We derive it
 * from the current M3 color scheme so the app respects dark theme and any
 * future dynamic-color opt-in without hard-coding brand hex values in the
 * chart layer.
 */
object ChartPalette {

    @Composable
    @ReadOnlyComposable
    fun categorical(): List<Color> = with(MaterialTheme.colorScheme) {
        listOf(
            primary,
            tertiary,
            secondary,
            primaryContainer.blend(onPrimaryContainer, 0.35f),
            tertiaryContainer.blend(onTertiaryContainer, 0.35f),
            secondaryContainer.blend(onSecondaryContainer, 0.35f),
            primary.copy(alpha = 0.6f),
            tertiary.copy(alpha = 0.6f)
        )
    }

    fun colorFor(index: Int, palette: List<Color>): Color = palette[((index % palette.size) + palette.size) % palette.size]

    private fun Color.blend(other: Color, fraction: Float): Color {
        val f = fraction.coerceIn(0f, 1f)
        return Color(
            red = red * (1f - f) + other.red * f,
            green = green * (1f - f) + other.green * f,
            blue = blue * (1f - f) + other.blue * f,
            alpha = alpha * (1f - f) + other.alpha * f
        )
    }
}
