package com.example.pocketpilot.feature.finance.presentation.budget.form

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod

@Immutable
data class BudgetFormState(
    val mode: BudgetFormMode = BudgetFormMode.Add,
    val name: String = "",
    val limitInput: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val category: String = "",
    val startsAtMillis: Long = System.currentTimeMillis(),
    val endsAtMillis: Long? = null,
    val currencyCode: String = "USD",
    val nameError: String? = null,
    val limitError: String? = null,
    val categoryError: String? = null,
    val dateError: String? = null,
    val isLoadingExisting: Boolean = false,
    val isSubmitting: Boolean = false,
    val loadError: String? = null,
    val submitError: String? = null
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
            !isLoadingExisting &&
            name.isNotBlank() &&
            limitInput.isNotBlank()

    val isEdit: Boolean get() = mode is BudgetFormMode.Edit
}

@Immutable
sealed interface BudgetFormMode {
    data object Add : BudgetFormMode

    @Immutable data class Edit(val id: String) : BudgetFormMode
}

sealed interface BudgetFormEvent : UiEvent {
    data class NameChanged(val value: String) : BudgetFormEvent
    data class LimitChanged(val value: String) : BudgetFormEvent
    data class PeriodChanged(val value: BudgetPeriod) : BudgetFormEvent
    data class CategoryChanged(val value: String) : BudgetFormEvent
    data class StartDateChanged(val millis: Long) : BudgetFormEvent
    data class EndDateChanged(val millis: Long?) : BudgetFormEvent
    data object Submit : BudgetFormEvent
    data object Cancel : BudgetFormEvent
    data object Retry : BudgetFormEvent
}

sealed interface BudgetFormEffect : UiEffect {
    data object Saved : BudgetFormEffect
    data object Cancelled : BudgetFormEffect
    data class ShowError(val message: String) : BudgetFormEffect
}
