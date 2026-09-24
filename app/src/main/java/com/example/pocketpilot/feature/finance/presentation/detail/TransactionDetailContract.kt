package com.example.pocketpilot.feature.finance.presentation.detail

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.core.ui.base.UiEffect
import com.example.pocketpilot.core.ui.base.UiEvent
import com.example.pocketpilot.feature.finance.domain.model.Transaction

@Immutable
data class TransactionDetailState(
    val transaction: UiState<Transaction> = UiState.Loading,
    val isDeleting: Boolean = false,
    val showDeleteConfirmation: Boolean = false
)

sealed interface TransactionDetailEvent : UiEvent {
    data object EditClicked : TransactionDetailEvent
    data object DeleteClicked : TransactionDetailEvent
    data object DeleteConfirmed : TransactionDetailEvent
    data object DeleteDismissed : TransactionDetailEvent
    data object BackClicked : TransactionDetailEvent
    data object Retry : TransactionDetailEvent
}

sealed interface TransactionDetailEffect : UiEffect {
    data class NavigateToEdit(val id: String) : TransactionDetailEffect
    data object NavigateBack : TransactionDetailEffect
    data class ShowError(val message: String) : TransactionDetailEffect
}
