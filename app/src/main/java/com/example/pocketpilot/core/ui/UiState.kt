package com.example.pocketpilot.core.ui

import androidx.compose.runtime.Immutable
import com.example.pocketpilot.core.common.Result

@Immutable
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>

    @Immutable data class Success<T>(val data: T) : UiState<T>

    @Immutable data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
}

fun <T> Result<T>.toUiState(
    isEmpty: (T) -> Boolean = { false },
    errorMessage: (Throwable) -> String = { it.message ?: "Unknown error" }
): UiState<T> = when (this) {
    Result.Loading -> UiState.Loading
    is Result.Success -> if (isEmpty(data)) UiState.Empty else UiState.Success(data)
    is Result.Error -> UiState.Error(errorMessage(throwable), throwable)
}
