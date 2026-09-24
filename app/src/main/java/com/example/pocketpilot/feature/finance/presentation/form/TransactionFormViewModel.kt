package com.example.pocketpilot.feature.finance.presentation.form

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import com.example.pocketpilot.feature.finance.domain.usecase.AddTransactionUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.EditTransactionUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.GetTransactionUseCase
import com.example.pocketpilot.feature.finance.domain.validation.TransactionValidator
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter
import kotlinx.coroutines.launch
import java.util.UUID

class TransactionFormViewModel(
    initialMode: FormMode,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val editTransactionUseCase: EditTransactionUseCase,
    private val getTransactionUseCase: GetTransactionUseCase
) : BaseViewModel<TransactionFormState, TransactionFormEvent, TransactionFormEffect>(
    TransactionFormState(mode = initialMode)
) {

    init {
        if (initialMode is FormMode.Edit) loadExisting(initialMode.id)
    }

    override fun handleEvent(event: TransactionFormEvent) {
        when (event) {
            is TransactionFormEvent.TitleChanged -> setState {
                copy(title = event.value, titleError = null, submitError = null)
            }
            is TransactionFormEvent.AmountChanged -> setState {
                copy(amountInput = event.value, amountError = null, submitError = null)
            }
            is TransactionFormEvent.TypeChanged -> setState { copy(type = event.value) }
            is TransactionFormEvent.CategoryChanged -> setState { copy(category = event.value) }
            is TransactionFormEvent.NoteChanged -> setState {
                copy(note = event.value, noteError = null)
            }
            is TransactionFormEvent.DateChanged -> setState { copy(occurredAtMillis = event.millis) }
            TransactionFormEvent.Submit -> submit()
            TransactionFormEvent.Cancel -> sendEffect(TransactionFormEffect.Cancelled)
            TransactionFormEvent.Retry -> {
                val mode = currentState.mode
                if (mode is FormMode.Edit) loadExisting(mode.id)
            }
        }
    }

    private fun loadExisting(id: String) {
        setState { copy(isLoadingExisting = true, loadError = null) }
        viewModelScope.launch {
            runCatching { getTransactionUseCase(id) }
                .onSuccess { transaction ->
                    if (transaction == null) {
                        setState {
                            copy(
                                isLoadingExisting = false,
                                loadError = "Transaction not found"
                            )
                        }
                    } else {
                        val amountDisplay = MoneyFormatter.format(
                            minorUnits = transaction.amountMinorUnits,
                            currencyCode = transaction.currencyCode
                        ).filter { it.isDigit() || it == '.' || it == ',' }
                            .ifBlank { (transaction.amountMinorUnits / 100.0).toString() }
                        setState {
                            copy(
                                isLoadingExisting = false,
                                title = transaction.title,
                                amountInput = amountDisplay,
                                type = transaction.type,
                                category = transaction.categoryId.orEmpty(),
                                note = transaction.note.orEmpty(),
                                occurredAtMillis = transaction.occurredAtEpochMillis,
                                currencyCode = transaction.currencyCode
                            )
                        }
                    }
                }
                .onFailure { t ->
                    setState {
                        copy(
                            isLoadingExisting = false,
                            loadError = t.message ?: "Failed to load transaction"
                        )
                    }
                }
        }
    }

    private fun submit() {
        val s = currentState
        val titleError = TransactionValidator.validateTitle(s.title)?.message
        val amountError = TransactionValidator.validateAmount(s.amountInput)?.message
        val noteError = TransactionValidator.validateNote(s.note.takeIf { it.isNotBlank() })?.message
        if (titleError != null || amountError != null || noteError != null) {
            setState {
                copy(
                    titleError = titleError,
                    amountError = amountError,
                    noteError = noteError
                )
            }
            return
        }
        val minorUnits = MoneyFormatter.parseToMinorUnits(s.amountInput, s.currencyCode)
        if (minorUnits == null) {
            setState { copy(amountError = "Enter a valid number") }
            return
        }
        setState { copy(isSubmitting = true, submitError = null) }
        val now = System.currentTimeMillis()
        val mode = s.mode
        val transaction = Transaction(
            id = if (mode is FormMode.Edit) mode.id else UUID.randomUUID().toString(),
            title = s.title.trim(),
            amountMinorUnits = minorUnits,
            currencyCode = s.currencyCode,
            type = s.type,
            categoryId = s.category.trim().takeIf { it.isNotBlank() },
            budgetId = null,
            occurredAtEpochMillis = s.occurredAtMillis,
            note = s.note.trim().takeIf { it.isNotBlank() },
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )
        viewModelScope.launch {
            val result = runCatching {
                if (mode is FormMode.Edit) {
                    editTransactionUseCase(transaction)
                } else {
                    addTransactionUseCase(transaction)
                }
            }
            result
                .onSuccess {
                    setState { copy(isSubmitting = false) }
                    sendEffect(TransactionFormEffect.Saved)
                }
                .onFailure { t ->
                    val message = t.message ?: "Failed to save transaction"
                    setState { copy(isSubmitting = false, submitError = message) }
                    sendEffect(TransactionFormEffect.ShowError(message))
                }
        }
    }
}
