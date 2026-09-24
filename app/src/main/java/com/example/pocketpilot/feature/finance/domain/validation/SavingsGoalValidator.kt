package com.example.pocketpilot.feature.finance.domain.validation

/**
 * Field-level validation for the savings goal form. Mirrors [BudgetValidator]
 * so presentation code can consume both the same way.
 */
object SavingsGoalValidator {

    private const val MAX_NAME_LENGTH = 60
    private const val MAX_NOTE_LENGTH = 240

    fun validateName(raw: String): SavingsGoalFieldError? = when {
        raw.isBlank() -> SavingsGoalFieldError.NameRequired
        raw.length > MAX_NAME_LENGTH -> SavingsGoalFieldError.NameTooLong(MAX_NAME_LENGTH)
        else -> null
    }

    fun validateTarget(raw: String): SavingsGoalFieldError? {
        if (raw.isBlank()) return SavingsGoalFieldError.TargetRequired
        val normalized = raw.trim().replace(",", ".")
        val parsed = normalized.toDoubleOrNull() ?: return SavingsGoalFieldError.TargetInvalid
        if (parsed <= 0.0) return SavingsGoalFieldError.TargetNonPositive
        if (parsed >= 1_000_000_000.0) return SavingsGoalFieldError.TargetTooLarge
        return null
    }

    fun validateContribution(raw: String): SavingsGoalFieldError? {
        if (raw.isBlank()) return SavingsGoalFieldError.ContributionRequired
        val normalized = raw.trim().replace(",", ".")
        val parsed = normalized.toDoubleOrNull() ?: return SavingsGoalFieldError.ContributionInvalid
        if (parsed <= 0.0) return SavingsGoalFieldError.ContributionNonPositive
        return null
    }

    fun validateNote(raw: String?): SavingsGoalFieldError? {
        val text = raw ?: return null
        return if (text.length > MAX_NOTE_LENGTH) {
            SavingsGoalFieldError.NoteTooLong(MAX_NOTE_LENGTH)
        } else {
            null
        }
    }

    fun validateTargetDate(targetDateMillis: Long?, nowMillis: Long): SavingsGoalFieldError? {
        if (targetDateMillis != null && targetDateMillis < nowMillis) {
            return SavingsGoalFieldError.TargetDateInPast
        }
        return null
    }
}

sealed interface SavingsGoalFieldError {
    val message: String

    data object NameRequired : SavingsGoalFieldError {
        override val message: String = "Name is required"
    }
    data class NameTooLong(val max: Int) : SavingsGoalFieldError {
        override val message: String = "Name must be $max characters or fewer"
    }
    data object TargetRequired : SavingsGoalFieldError {
        override val message: String = "Target is required"
    }
    data object TargetInvalid : SavingsGoalFieldError {
        override val message: String = "Enter a valid number"
    }
    data object TargetNonPositive : SavingsGoalFieldError {
        override val message: String = "Target must be greater than zero"
    }
    data object TargetTooLarge : SavingsGoalFieldError {
        override val message: String = "Target is too large"
    }
    data object ContributionRequired : SavingsGoalFieldError {
        override val message: String = "Amount is required"
    }
    data object ContributionInvalid : SavingsGoalFieldError {
        override val message: String = "Enter a valid number"
    }
    data object ContributionNonPositive : SavingsGoalFieldError {
        override val message: String = "Amount must be greater than zero"
    }
    data class NoteTooLong(val max: Int) : SavingsGoalFieldError {
        override val message: String = "Note must be $max characters or fewer"
    }
    data object TargetDateInPast : SavingsGoalFieldError {
        override val message: String = "Target date must be in the future"
    }
}
