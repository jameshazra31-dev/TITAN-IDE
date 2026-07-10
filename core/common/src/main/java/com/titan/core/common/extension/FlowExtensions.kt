package com.titan.core.common.extension

import com.titan.core.common.base.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

fun <T> flowOf(block: suspend () -> T): Flow<Result<T>> = flow {
    emit(Result.Success(block()))
}.onStart { emit(Result.Loading) }
 .catch { emit(Result.Error(it)) }
 .onCompletion { }

fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .mapSuccess { Result.Success(it) }
    .onStart { emit(Result.Loading) }
    .catch { emit(Result.Error(it)) }

private fun <T> Flow<T>.mapSuccess(transform: (T) -> Result<T>): Flow<Result<T>> = flow {
    collect { value -> emit(transform(value)) }
}