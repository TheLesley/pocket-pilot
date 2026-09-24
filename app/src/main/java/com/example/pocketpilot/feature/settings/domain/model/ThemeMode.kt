package com.example.pocketpilot.feature.settings.domain.model

/**
 * User's preferred theme. [SYSTEM] defers to the OS setting so the value is
 * still meaningful before the user has expressed an explicit preference.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}
