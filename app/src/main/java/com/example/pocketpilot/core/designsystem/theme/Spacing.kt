package com.example.pocketpilot.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard spacing scale for the app. Use these instead of raw `dp` literals
 * so that layouts stay consistent and can be tuned in one place.
 */
data class Spacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val xxxl: Dp = 64.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

val LocalPocketPilotColors = staticCompositionLocalOf { LightExtendedColors }
