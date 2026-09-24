package com.example.pocketpilot.feature.security.domain.model

import androidx.compose.runtime.Immutable

/**
 * Validation outcome for a candidate PIN. The [Invalid] cases are exhaustive
 * so the UI can present a targeted error message instead of a generic string.
 */
@Immutable
sealed interface PinValidation {
    data object Valid : PinValidation

    sealed interface Invalid : PinValidation {
        data object TooShort : Invalid
        data object TooLong : Invalid
        data object NotDigits : Invalid
    }

    companion object {
        const val MinLength: Int = 4
        const val MaxLength: Int = 8

        fun validate(pin: String): PinValidation = when {
            pin.length < MinLength -> Invalid.TooShort
            pin.length > MaxLength -> Invalid.TooLong
            !pin.all { it.isDigit() } -> Invalid.NotDigits
            else -> Valid
        }
    }
}
