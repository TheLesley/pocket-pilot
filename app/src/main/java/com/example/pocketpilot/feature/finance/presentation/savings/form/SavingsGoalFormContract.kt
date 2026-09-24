package com.example.pocketpilot.feature.finance.presentation.savings.form

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent

@Immutable
data class SavingsGoalFormState(
    val mode: SavingsGoalFormMode = SavingsGoalFormMode.Add,
    val name: String = "",
    val targetInput: String = "",
    val currencyCode: String = "USD",
    val targetDateMillis: Long? = null,
    val note: String = "",
    val nameError: String? = null,
    val targetError: String? = null,
    val noteError: String? = null,
    val targetDateError: String? = null,
    val isLoadingExisting: Boolean = false,
    val isSubmitting: Boolean = false,
    val loadError: String? = null,
    val submitError: String? = null
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
            !isLoadingExisting &&
            name.isNotBlank() &&
            targetInput.isNotBlank()

    val isEdit: Boolean get() = mode is SavingsGoalFormMode.Edit
}

@Immutable
sealed interface SavingsGoalFormMode {
    data object Add : SavingsGoalFormMode

    @Immutable data class Edit(val id: String) : SavingsGoalFormMode
}

sealed interface SavingsGoalFormEvent : UiEvent {
    data class NameChanged(val value: String) : SavingsGoalFormEvent
    data class TargetChanged(val value: String) : SavingsGoalFormEvent
    data class NoteChanged(val value: String) : SavingsGoalFormEvent
    data class TargetDateChanged(val millis: Long?) : SavingsGoalFormEvent
    data object Submit : SavingsGoalFormEvent
    data object Cancel : SavingsGoalFormEvent
    data object Retry : SavingsGoalFormEvent
}

sealed interface SavingsGoalFormEffect : UiEffect {
    data object Saved : SavingsGoalFormEffect
    data object Cancelled : SavingsGoalFormEffect
    data class ShowError(val message: String) : SavingsGoalFormEffect
}
