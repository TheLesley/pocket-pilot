package com.example.pocketpilot.feature.finance.presentation.form

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

@Immutable
data class TransactionFormState(
    val mode: FormMode = FormMode.Add,
    val title: String = "",
    val amountInput: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val note: String = "",
    val occurredAtMillis: Long = System.currentTimeMillis(),
    val currencyCode: String = "USD",
    val titleError: String? = null,
    val amountError: String? = null,
    val noteError: String? = null,
    val isLoadingExisting: Boolean = false,
    val isSubmitting: Boolean = false,
    val loadError: String? = null,
    val submitError: String? = null
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
            !isLoadingExisting &&
            title.isNotBlank() &&
            amountInput.isNotBlank()

    val isEdit: Boolean get() = mode is FormMode.Edit
}

@Immutable
sealed interface FormMode {
    data object Add : FormMode

    @Immutable data class Edit(val id: String) : FormMode
}

sealed interface TransactionFormEvent : UiEvent {
    data class TitleChanged(val value: String) : TransactionFormEvent
    data class AmountChanged(val value: String) : TransactionFormEvent
    data class TypeChanged(val value: TransactionType) : TransactionFormEvent
    data class CategoryChanged(val value: String) : TransactionFormEvent
    data class NoteChanged(val value: String) : TransactionFormEvent
    data class DateChanged(val millis: Long) : TransactionFormEvent
    data object Submit : TransactionFormEvent
    data object Cancel : TransactionFormEvent
    data object Retry : TransactionFormEvent
}

sealed interface TransactionFormEffect : UiEffect {
    data object Saved : TransactionFormEffect
    data object Cancelled : TransactionFormEffect
    data class ShowError(val message: String) : TransactionFormEffect
}
