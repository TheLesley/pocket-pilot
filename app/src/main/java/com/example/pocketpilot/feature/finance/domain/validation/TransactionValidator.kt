package com.example.pocketpilot.feature.finance.domain.validation

/**
 * Field-level validation for the transaction form. Presentation collects the
 * result into a `Map<Field, Error>` so it can surface per-field messages.
 */
object TransactionValidator {

    private const val MAX_TITLE_LENGTH = 80
    private const val MAX_NOTE_LENGTH = 240

    fun validateTitle(raw: String): TransactionFieldError? = when {
        raw.isBlank() -> TransactionFieldError.TitleRequired
        raw.length > MAX_TITLE_LENGTH -> TransactionFieldError.TitleTooLong(MAX_TITLE_LENGTH)
        else -> null
    }

    fun validateAmount(raw: String): TransactionFieldError? {
        if (raw.isBlank()) return TransactionFieldError.AmountRequired
        val normalized = raw.trim().replace(",", ".")
        val parsed = normalized.toDoubleOrNull() ?: return TransactionFieldError.AmountInvalid
        if (parsed <= 0.0) return TransactionFieldError.AmountNonPositive
        if (parsed >= 1_000_000_000.0) return TransactionFieldError.AmountTooLarge
        return null
    }

    fun validateNote(raw: String?): TransactionFieldError? {
        val text = raw ?: return null
        return if (text.length > MAX_NOTE_LENGTH) {
            TransactionFieldError.NoteTooLong(MAX_NOTE_LENGTH)
        } else {
            null
        }
    }
}

sealed interface TransactionFieldError {
    val message: String

    data object TitleRequired : TransactionFieldError {
        override val message: String = "Title is required"
    }
    data class TitleTooLong(val max: Int) : TransactionFieldError {
        override val message: String = "Title must be $max characters or fewer"
    }
    data object AmountRequired : TransactionFieldError {
        override val message: String = "Amount is required"
    }
    data object AmountInvalid : TransactionFieldError {
        override val message: String = "Enter a valid number"
    }
    data object AmountNonPositive : TransactionFieldError {
        override val message: String = "Amount must be greater than zero"
    }
    data object AmountTooLarge : TransactionFieldError {
        override val message: String = "Amount is too large"
    }
    data class NoteTooLong(val max: Int) : TransactionFieldError {
        override val message: String = "Note must be $max characters or fewer"
    }
}
