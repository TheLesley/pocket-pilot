package com.example.pocketpilot.feature.finance.presentation.savings.form

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal
import com.example.pocketpilot.feature.finance.domain.usecase.AddSavingsGoalUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.EditSavingsGoalUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.GetSavingsGoalUseCase
import com.example.pocketpilot.feature.finance.domain.validation.SavingsGoalValidator
import com.example.pocketpilot.feature.finance.presentation.util.MoneyFormatter
import kotlinx.coroutines.launch
import java.util.UUID

class SavingsGoalFormViewModel(
    initialMode: SavingsGoalFormMode,
    private val addSavingsGoalUseCase: AddSavingsGoalUseCase,
    private val editSavingsGoalUseCase: EditSavingsGoalUseCase,
    private val getSavingsGoalUseCase: GetSavingsGoalUseCase
) : BaseViewModel<SavingsGoalFormState, SavingsGoalFormEvent, SavingsGoalFormEffect>(
    SavingsGoalFormState(mode = initialMode)
) {

    /**
     * Existing saved balance for the goal being edited; preserved so an edit
     * doesn't accidentally clobber contributions the user has already made.
     */
    private var existingSavedMinorUnits: Long = 0L
    private var existingCreatedAtMillis: Long? = null

    init {
        if (initialMode is SavingsGoalFormMode.Edit) loadExisting(initialMode.id)
    }

    override fun handleEvent(event: SavingsGoalFormEvent) {
        when (event) {
            is SavingsGoalFormEvent.NameChanged -> setState {
                copy(name = event.value, nameError = null, submitError = null)
            }
            is SavingsGoalFormEvent.TargetChanged -> setState {
                copy(targetInput = event.value, targetError = null, submitError = null)
            }
            is SavingsGoalFormEvent.NoteChanged -> setState {
                copy(note = event.value, noteError = null)
            }
            is SavingsGoalFormEvent.TargetDateChanged -> setState {
                copy(targetDateMillis = event.millis, targetDateError = null)
            }
            SavingsGoalFormEvent.Submit -> submit()
            SavingsGoalFormEvent.Cancel -> sendEffect(SavingsGoalFormEffect.Cancelled)
            SavingsGoalFormEvent.Retry -> {
                val mode = currentState.mode
                if (mode is SavingsGoalFormMode.Edit) loadExisting(mode.id)
            }
        }
    }

    private fun loadExisting(id: String) {
        setState { copy(isLoadingExisting = true, loadError = null) }
        viewModelScope.launch {
            runCatching { getSavingsGoalUseCase(id) }
                .onSuccess { goal ->
                    if (goal == null) {
                        setState {
                            copy(
                                isLoadingExisting = false,
                                loadError = "Savings goal not found"
                            )
                        }
                    } else {
                        existingSavedMinorUnits = goal.savedMinorUnits
                        existingCreatedAtMillis = goal.createdAtEpochMillis
                        val targetDisplay = (goal.targetMinorUnits / 100.0).toString()
                        setState {
                            copy(
                                isLoadingExisting = false,
                                name = goal.name,
                                targetInput = targetDisplay,
                                currencyCode = goal.currencyCode,
                                targetDateMillis = goal.targetDateEpochMillis,
                                note = goal.note.orEmpty()
                            )
                        }
                    }
                }
                .onFailure { t ->
                    setState {
                        copy(
                            isLoadingExisting = false,
                            loadError = t.message ?: "Failed to load savings goal"
                        )
                    }
                }
        }
    }

    private fun submit() {
        val s = currentState
        val now = System.currentTimeMillis()
        val nameError = SavingsGoalValidator.validateName(s.name)?.message
        val targetError = SavingsGoalValidator.validateTarget(s.targetInput)?.message
        val noteError = SavingsGoalValidator.validateNote(
            s.note.takeIf { it.isNotBlank() }
        )?.message
        val dateError = SavingsGoalValidator.validateTargetDate(s.targetDateMillis, now)?.message
        if (nameError != null || targetError != null || noteError != null || dateError != null) {
            setState {
                copy(
                    nameError = nameError,
                    targetError = targetError,
                    noteError = noteError,
                    targetDateError = dateError
                )
            }
            return
        }
        val minorUnits = MoneyFormatter.parseToMinorUnits(s.targetInput, s.currencyCode)
        if (minorUnits == null) {
            setState { copy(targetError = "Enter a valid number") }
            return
        }
        setState { copy(isSubmitting = true, submitError = null) }
        val mode = s.mode
        val goal = SavingsGoal(
            id = if (mode is SavingsGoalFormMode.Edit) mode.id else UUID.randomUUID().toString(),
            name = s.name.trim(),
            targetMinorUnits = minorUnits,
            savedMinorUnits = if (mode is SavingsGoalFormMode.Edit) existingSavedMinorUnits else 0L,
            currencyCode = s.currencyCode,
            targetDateEpochMillis = s.targetDateMillis,
            note = s.note.trim().takeIf { it.isNotBlank() },
            createdAtEpochMillis = existingCreatedAtMillis ?: now,
            updatedAtEpochMillis = now
        )
        viewModelScope.launch {
            val result = runCatching {
                if (mode is SavingsGoalFormMode.Edit) {
                    editSavingsGoalUseCase(goal)
                } else {
                    addSavingsGoalUseCase(goal)
                }
            }
            result
                .onSuccess {
                    setState { copy(isSubmitting = false) }
                    sendEffect(SavingsGoalFormEffect.Saved)
                }
                .onFailure { t ->
                    val message = t.message ?: "Failed to save savings goal"
                    setState { copy(isSubmitting = false, submitError = message) }
                    sendEffect(SavingsGoalFormEffect.ShowError(message))
                }
        }
    }
}
