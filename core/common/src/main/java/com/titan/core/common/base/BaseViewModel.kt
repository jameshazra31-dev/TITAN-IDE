package com.titan.core.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : UiState, E : UiEvent, A : UiAction> : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<S> = _state.asStateFlow()

    private val _action = MutableSharedFlow<A>()
    val action: SharedFlow<A> = _action.asSharedFlow()

    private val _event = Channel<E>()
    val event = _event.receiveAsFlow()

    protected abstract fun initialState(): S

    protected fun setState(reducer: S.() -> S) {
        _state.value = _state.value.reducer()
    }

    protected fun sendAction(action: A) {
        viewModelScope.launch { _action.emit(action) }
    }

    protected fun sendEvent(event: E) {
        viewModelScope.launch { _event.send(event) }
    }

    abstract fun onEvent(event: E)
}

interface UiState

interface UiEvent

interface UiAction