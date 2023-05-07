package com.alexredchets.belkaplayer

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

fun <T : Any> Flow<State<T>>.applyCommonHandling(
    doOnStart: (() -> Unit)? = null,
    doOnComplete: (() -> Unit)? = null,
    doOnError: ((Throwable) -> Unit)? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = flowOn(dispatcher)
    .catch {
        emit(State.Progress(isLoading = false))
        doOnError?.invoke(it)
    }
    .onStart {
        emit(State.Progress(isLoading = true))
        doOnStart?.invoke()
    }
    .onCompletion {
        emit(State.Progress(isLoading = false))
        doOnComplete?.invoke()
    }
    .flowOn(Dispatchers.Main)

sealed class State<out T: Any> {
    data class Success<out T: Any>(val data: T): State<T>()
    data class Failure(val exception: Exception): State<Nothing>()
    data class Progress(val isLoading: Boolean): State<Nothing>()
}
