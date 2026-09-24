package com.example.pocketpilot.feature.finance.presentation.budget.form

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.finance.domain.model.Budget
import com.example.pocketpilot.feature.finance.domain.usecase.AddBudgetUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.EditBudgetUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.GetBudgetUseCase
import com.example.pocketpilot.feature.finance.domain.validation.BudgetValidator
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter
import kotlinx.coroutines.launch
import java.util.UUID

class BudgetFormViewModel(
    initialMode: BudgetFormMode,
    private val addBudgetUseCase: AddBudgetUseCase,
    private val editBudgetUseCase: EditBudgetUseCase,
    private val getBudgetUseCase: GetBudgetUseCase
) : BaseViewModel<BudgetFormState, BudgetFormEvent, BudgetFormEffect>(
    BudgetFormState(mode = initialMode)
) {

    init {
        if (initialMode is BudgetFormMode.Edit) loadExisting(initialMode.id)
    }

    override fun handleEvent(event: BudgetFormEvent) {
        when (event) {
            is BudgetFormEvent.NameChanged -> setState {
                copy(name = event.value, nameError = null, submitError = null)
            }
            is BudgetFormEvent.LimitChanged -> setState {
                copy(limitInput = event.value, limitError = null, submitError = null)
            }
            is BudgetFormEvent.PeriodChanged -> setState { copy(period = event.value) }
            is BudgetFormEvent.CategoryChanged -> setState {
                copy(category = event.value, categoryError = null)
            }
            is BudgetFormEvent.StartDateChanged -> setState {
                copy(startsAtMillis = event.millis, dateError = null)
            }
            is BudgetFormEvent.EndDateChanged -> setState {
                copy(endsAtMillis = event.millis, dateError = null)
            }
            BudgetFormEvent.Submit -> submit()
            BudgetFormEvent.Cancel -> sendEffect(BudgetFormEffect.Cancelled)
            BudgetFormEvent.Retry -> {
                val mode = currentState.mode
                if (mode is BudgetFormMode.Edit) loadExisting(mode.id)
            }
        }
    }

    private fun loadExisting(id: String) {
        setState { copy(isLoadingExisting = true, loadError = null) }
        viewModelScope.launch {
            runCatching { getBudgetUseCase(id) }
                .onSuccess { budget ->
                    if (budget == null) {
                        setState {
                            copy(
                                isLoadingExisting = false,
                                loadError = "Budget not found"
                            )
                        }
                    } else {
                        val limitDisplay = MoneyFormatter.format(
                            minorUnits = budget.limitMinorUnits,
                            currencyCode = budget.currencyCode
                        ).filter { it.isDigit() || it == '.' || it == ',' }
                            .ifBlank { (budget.limitMinorUnits / 100.0).toString() }
                        setState {
                            copy(
                                isLoadingExisting = false,
                                name = budget.name,
                                limitInput = limitDisplay,
                                period = budget.period,
                                category = budget.categoryId.orEmpty(),
                                startsAtMillis = budget.startsAtEpochMillis,
                                endsAtMillis = budget.endsAtEpochMillis,
                                currencyCode = budget.currencyCode
                            )
                        }
                    }
                }
                .onFailure { t ->
                    setState {
                        copy(
                            isLoadingExisting = false,
                            loadError = t.message ?: "Failed to load budget"
                        )
                    }
                }
        }
    }

    private fun submit() {
        val s = currentState
        val nameError = BudgetValidator.validateName(s.name)?.message
        val limitError = BudgetValidator.validateLimit(s.limitInput)?.message
        val categoryError = BudgetValidator.validateCategory(s.category.takeIf { it.isNotBlank() })?.message
        val dateError = BudgetValidator.validateDateRange(s.startsAtMillis, s.endsAtMillis)?.message
        if (nameError != null || limitError != null || categoryError != null || dateError != null) {
            setState {
                copy(
                    nameError = nameError,
                    limitError = limitError,
                    categoryError = categoryError,
                    dateError = dateError
                )
            }
            return
        }
        val minorUnits = MoneyFormatter.parseToMinorUnits(s.limitInput, s.currencyCode)
        if (minorUnits == null) {
            setState { copy(limitError = "Enter a valid number") }
            return
        }
        setState { copy(isSubmitting = true, submitError = null) }
        val now = System.currentTimeMillis()
        val mode = s.mode
        val budget = Budget(
            id = if (mode is BudgetFormMode.Edit) mode.id else UUID.randomUUID().toString(),
            name = s.name.trim(),
            limitMinorUnits = minorUnits,
            currencyCode = s.currencyCode,
            period = s.period,
            startsAtEpochMillis = s.startsAtMillis,
            endsAtEpochMillis = s.endsAtMillis,
            categoryId = s.category.trim().takeIf { it.isNotBlank() },
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )
        viewModelScope.launch {
            val result = runCatching {
                if (mode is BudgetFormMode.Edit) {
                    editBudgetUseCase(budget)
                } else {
                    addBudgetUseCase(budget)
                }
            }
            result
                .onSuccess {
                    setState { copy(isSubmitting = false) }
                    sendEffect(BudgetFormEffect.Saved)
                }
                .onFailure { t ->
                    val message = t.message ?: "Failed to save budget"
                    setState { copy(isSubmitting = false, submitError = message) }
                    sendEffect(BudgetFormEffect.ShowError(message))
                }
        }
    }
}
