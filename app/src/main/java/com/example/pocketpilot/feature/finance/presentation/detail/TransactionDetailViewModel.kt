package com.example.pocketpilot.feature.finance.presentation.detail

import androidx.lifecycle.viewModelScope
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.BaseViewModel
import com.example.pocketpilot.feature.finance.domain.usecase.DeleteTransactionUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.ObserveTransactionUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val transactionId: String,
    private val observeTransactionUseCase: ObserveTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : BaseViewModel<TransactionDetailState, TransactionDetailEvent, TransactionDetailEffect>(TransactionDetailState()) {

    init {
        observeTransaction()
    }

    override fun handleEvent(event: TransactionDetailEvent) {
        when (event) {
            TransactionDetailEvent.EditClicked ->
                sendEffect(TransactionDetailEffect.NavigateToEdit(transactionId))
            TransactionDetailEvent.DeleteClicked -> setState { copy(showDeleteConfirmation = true) }
            TransactionDetailEvent.DeleteDismissed -> setState { copy(showDeleteConfirmation = false) }
            TransactionDetailEvent.DeleteConfirmed -> delete()
            TransactionDetailEvent.BackClicked -> sendEffect(TransactionDetailEffect.NavigateBack)
            TransactionDetailEvent.Retry -> observeTransaction()
        }
    }

    private fun observeTransaction() {
        observeTransactionUseCase(transactionId)
            .onStart { setState { copy(transaction = UiState.Loading) } }
            .catch { t ->
                setState {
                    copy(
                        transaction = UiState.Error(
                            message = t.message ?: "Failed to load transaction",
                            throwable = t
                        )
                    )
                }
            }
            .onEach { tx ->
                setState {
                    copy(
                        transaction = when (tx) {
                            null -> UiState.Empty
                            else -> UiState.Success(tx)
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun delete() {
        setState { copy(isDeleting = true, showDeleteConfirmation = false) }
        viewModelScope.launch {
            runCatching { deleteTransactionUseCase(transactionId) }
                .onSuccess {
                    setState { copy(isDeleting = false) }
                    sendEffect(TransactionDetailEffect.NavigateBack)
                }
                .onFailure { t ->
                    setState { copy(isDeleting = false) }
                    sendEffect(TransactionDetailEffect.ShowError(t.message ?: "Delete failed"))
                }
        }
    }
}
