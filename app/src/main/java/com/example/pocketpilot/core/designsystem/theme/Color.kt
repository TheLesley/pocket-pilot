package com.example.pocketpilot.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// --- Brand palette ---------------------------------------------------------
private val Teal10 = Color(0xFF002020)
private val Teal20 = Color(0xFF003737)
private val Teal30 = Color(0xFF004F4F)
private val Teal40 = Color(0xFF006A6A)
private val Teal80 = Color(0xFF4FD8D8)
private val Teal90 = Color(0xFF6FF6F6)

private val Slate10 = Color(0xFF0F1417)
private val Slate20 = Color(0xFF1B2124)
private val Slate30 = Color(0xFF2A3135)
private val Slate40 = Color(0xFF3E464B)
private val Slate80 = Color(0xFFBFC8CE)
private val Slate90 = Color(0xFFDBE4EA)

private val Amber40 = Color(0xFF7C5800)
private val Amber80 = Color(0xFFF7BD48)

// --- Neutrals --------------------------------------------------------------
private val NeutralWhite = Color(0xFFFDFDFD)
private val NeutralGrey95 = Color(0xFFF1F3F4)
private val NeutralGrey20 = Color(0xFF2D3134)
private val NeutralGrey10 = Color(0xFF171A1C)

private val ErrorLight = Color(0xFFBA1A1A)
private val ErrorDark = Color(0xFFFFB4AB)

// --- Semantic finance colors (income/expense) ------------------------------
// Exposed via PocketPilotColors so screens can reference them regardless of theme.
private val IncomeLight = Color(0xFF1F7A3D)
private val IncomeDark = Color(0xFF7BDE9B)
private val ExpenseLight = Color(0xFFB3261E)
private val ExpenseDark = Color(0xFFFFB4AB)

internal val LightColorScheme = lightColorScheme(
    primary = Teal40,
    onPrimary = NeutralWhite,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal10,
    secondary = Slate40,
    onSecondary = NeutralWhite,
    secondaryContainer = Slate90,
    onSecondaryContainer = Slate10,
    tertiary = Amber40,
    onTertiary = NeutralWhite,
    tertiaryContainer = Amber80,
    onTertiaryContainer = Color(0xFF271900),
    background = NeutralWhite,
    onBackground = NeutralGrey10,
    surface = NeutralWhite,
    onSurface = NeutralGrey10,
    surfaceVariant = NeutralGrey95,
    onSurfaceVariant = Slate30,
    outline = Color(0xFF6F7A7F),
    outlineVariant = Color(0xFFBFC8CE),
    error = ErrorLight,
    onError = NeutralWhite
)

internal val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal10,
    primaryContainer = Teal30,
    onPrimaryContainer = Teal90,
    secondary = Slate80,
    onSecondary = Slate10,
    secondaryContainer = Slate30,
    onSecondaryContainer = Slate90,
    tertiary = Amber80,
    onTertiary = Color(0xFF412D00),
    tertiaryContainer = Color(0xFF5E4200),
    onTertiaryContainer = Color(0xFFFFDEA9),
    background = NeutralGrey10,
    onBackground = Slate90,
    surface = NeutralGrey10,
    onSurface = Slate90,
    surfaceVariant = NeutralGrey20,
    onSurfaceVariant = Slate80,
    outline = Color(0xFF89939A),
    outlineVariant = Color(0xFF3E464B),
    error = ErrorDark,
    onError = Color(0xFF690005)
)

/**
 * Extra semantic colors not covered by the Material 3 scheme.
 * Access via [LocalPocketPilotColors].
 */
data class PocketPilotColors(val income: Color, val expense: Color)

internal val LightExtendedColors = PocketPilotColors(
    income = IncomeLight,
    expense = ExpenseLight
)

internal val DarkExtendedColors = PocketPilotColors(
    income = IncomeDark,
    expense = ExpenseDark
)
