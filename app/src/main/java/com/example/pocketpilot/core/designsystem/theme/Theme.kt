package com.example.pocketpilot.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Root theme for PocketPilot.
 *
 * Dynamic color is intentionally disabled so the brand palette remains
 * consistent across devices — a portfolio app benefits from a recognisable
 * identity over per-device wallpaper theming.
 */
@Composable
fun PocketPilotTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalPocketPilotColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PocketPilotTypography,
            shapes = PocketPilotShapes,
            content = content
        )
    }
}

/**
 * Convenience accessors so screens can pull tokens with
 * `PocketPilotTheme.spacing.md` / `PocketPilotTheme.extendedColors.income`.
 */
object PocketPilotTheme {
    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current

    val extendedColors: PocketPilotColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPocketPilotColors.current
}
