package com.example.pocketpilot.feature.finance.domain.validation

/**
 * Field-level validation for the budget form. Mirrors the shape of
 * `TransactionValidator` so presentation code can consume both the same way.
 */
object BudgetValidator {

    private const val MAX_NAME_LENGTH = 60
    private const val MAX_CATEGORY_LENGTH = 40

    fun validateName(raw: String): BudgetFieldError? = when {
        raw.isBlank() -> BudgetFieldError.NameRequired
        raw.length > MAX_NAME_LENGTH -> BudgetFieldError.NameTooLong(MAX_NAME_LENGTH)
        else -> null
    }

    fun validateLimit(raw: String): BudgetFieldError? {
        if (raw.isBlank()) return BudgetFieldError.LimitRequired
        val normalized = raw.trim().replace(",", ".")
        val parsed = normalized.toDoubleOrNull() ?: return BudgetFieldError.LimitInvalid
        if (parsed <= 0.0) return BudgetFieldError.LimitNonPositive
        if (parsed >= 1_000_000_000.0) return BudgetFieldError.LimitTooLarge
        return null
    }

    fun validateCategory(raw: String?): BudgetFieldError? {
        val text = raw ?: return null
        return if (text.length > MAX_CATEGORY_LENGTH) {
            BudgetFieldError.CategoryTooLong(MAX_CATEGORY_LENGTH)
        } else {
            null
        }
    }

    fun validateDateRange(startsAt: Long, endsAt: Long?): BudgetFieldError? {
        if (endsAt != null && endsAt <= startsAt) return BudgetFieldError.EndBeforeStart
        return null
    }
}

sealed interface BudgetFieldError {
    val message: String

    data object NameRequired : BudgetFieldError {
        override val message: String = "Name is required"
    }
    data class NameTooLong(val max: Int) : BudgetFieldError {
        override val message: String = "Name must be $max characters or fewer"
    }
    data object LimitRequired : BudgetFieldError {
        override val message: String = "Limit is required"
    }
    data object LimitInvalid : BudgetFieldError {
        override val message: String = "Enter a valid number"
    }
    data object LimitNonPositive : BudgetFieldError {
        override val message: String = "Limit must be greater than zero"
    }
    data object LimitTooLarge : BudgetFieldError {
        override val message: String = "Limit is too large"
    }
    data class CategoryTooLong(val max: Int) : BudgetFieldError {
        override val message: String = "Category must be $max characters or fewer"
    }
    data object EndBeforeStart : BudgetFieldError {
        override val message: String = "End date must be after start date"
    }
}
