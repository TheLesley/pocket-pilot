package com.example.pocketpilot.core.common

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Immutable
sealed interface Result<out T> {
    @Immutable data class Success<T>(val data: T) : Result<T>

    @Immutable data class Error(val throwable: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    Result.Loading -> Result.Loading
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) action(throwable)
    return this
}

fun <T> Result<T>.getOrNull(): T? = (this as? Result.Success)?.data

fun <T> Flow<T>.asResult(): Flow<Result<T>> = map<T, Result<T>> { Result.Success(it) }
    .onStart { emit(Result.Loading) }
    .catch { emit(Result.Error(it)) }

inline fun <T> resultOf(block: () -> T): Result<T> = try {
    Result.Success(block())
} catch (t: Throwable) {
    Result.Error(t)
}

fun <T> resultFlow(block: suspend () -> T): Flow<Result<T>> = flow {
    emit(Result.Loading)
    emit(runCatching { block() }.fold(::Success, ::Error))
}

private fun <T> Success(value: T): Result<T> = Result.Success(value)
private fun <T> Error(t: Throwable): Result<T> = Result.Error(t)
