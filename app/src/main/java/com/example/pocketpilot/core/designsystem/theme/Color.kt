package com.example.pocketpilot.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// PocketPilot brand tokens — modern dark fintech palette.
// All raw hex values live here; screens should reference these tokens (or the
// Material3 colorScheme / PocketPilotColors) rather than hardcoding colors.
// ---------------------------------------------------------------------------

// Surfaces & structure
val AppBackground = Color(0xFF0F1117)
val SurfaceCard = Color(0xFF191C24)
val SurfaceElevated = Color(0xFF222631)
val BorderDivider = Color(0xFF272C38)

// Brand
val PrimaryBrand = Color(0xFF6366F1) // Indigo
val PrimaryPressed = Color(0xFF4F46E5)

// Semantic finance colors
val IncomePositive = Color(0xFF34D399)
val ExpenseNegative = Color(0xFFFB7185)
val ActionTransfer = Color(0xFF22D3EE)

// Text
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)

// Category chart palette
val CategoryFood = Color(0xFFF59E0B)
val CategoryTransport = Color(0xFF38BDF8)
val CategoryShopping = Color(0xFFA78BFA)
val CategoryBills = Color(0xFFFB7185)
val CategoryEntertainment = Color(0xFF34D399)
val CategoryOther = Color(0xFF94A3B8)

// ---------------------------------------------------------------------------
// Material 3 color schemes
// ---------------------------------------------------------------------------

internal val DarkColorScheme = darkColorScheme(
    primary = PrimaryBrand,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryPressed,
    onPrimaryContainer = TextPrimary,
    secondary = ActionTransfer,
    onSecondary = AppBackground,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = IncomePositive,
    onTertiary = AppBackground,
    tertiaryContainer = SurfaceElevated,
    onTertiaryContainer = TextPrimary,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceTint = PrimaryBrand,
    inverseSurface = TextPrimary,
    inverseOnSurface = AppBackground,
    outline = BorderDivider,
    outlineVariant = BorderDivider,
    error = ExpenseNegative,
    onError = TextPrimary,
    errorContainer = SurfaceElevated,
    onErrorContainer = ExpenseNegative,
    scrim = Color(0xCC000000)
)

// Light scheme retained for API completeness; the app is designed dark-first,
// so light mode mirrors the fintech tokens rather than a bespoke light palette.
internal val LightColorScheme = lightColorScheme(
    primary = PrimaryBrand,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryPressed,
    onPrimaryContainer = TextPrimary,
    secondary = ActionTransfer,
    onSecondary = AppBackground,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = IncomePositive,
    onTertiary = AppBackground,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderDivider,
    outlineVariant = BorderDivider,
    error = ExpenseNegative,
    onError = TextPrimary
)

/**
 * Extra semantic colors not covered by the Material 3 scheme.
 * Access via [LocalPocketPilotColors] or `PocketPilotTheme.extendedColors`.
 */
data class PocketPilotColors(
    val income: Color,
    val expense: Color,
    val transfer: Color,
    val border: Color,
    val surfaceElevated: Color,
    val textSecondary: Color,
    val categoryFood: Color,
    val categoryTransport: Color,
    val categoryShopping: Color,
    val categoryBills: Color,
    val categoryEntertainment: Color,
    val categoryOther: Color
) {
    val categoryPalette: List<Color>
        get() = listOf(
            categoryFood,
            categoryTransport,
            categoryShopping,
            categoryBills,
            categoryEntertainment,
            categoryOther
        )
}

private val FintechExtendedColors = PocketPilotColors(
    income = IncomePositive,
    expense = ExpenseNegative,
    transfer = ActionTransfer,
    border = BorderDivider,
    surfaceElevated = SurfaceElevated,
    textSecondary = TextSecondary,
    categoryFood = CategoryFood,
    categoryTransport = CategoryTransport,
    categoryShopping = CategoryShopping,
    categoryBills = CategoryBills,
    categoryEntertainment = CategoryEntertainment,
    categoryOther = CategoryOther
)

internal val LightExtendedColors = FintechExtendedColors
internal val DarkExtendedColors = FintechExtendedColors
