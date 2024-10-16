package com.anod.appwatcher.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseFlowViewModel<State, Event, Action> : ViewModel() {

    private lateinit var _viewState: MutableStateFlow<State>
    val viewStates: StateFlow<State>
        get() = _viewState

    var viewState: State
        get() {
            if (::_viewState.isInitialized) {
                return _viewState.value
            } else {
                throw UninitializedPropertyAccessException("\"viewState\" was queried before being initialized")
            }
        }
        protected set(value) {
            if (!::_viewState.isInitialized) {
                _viewState = MutableStateFlow(value)
            } else {
                _viewState.update { value }
            }
        }

    private var _viewActions = Channel<Action>()
    val viewActions: Flow<Action> = _viewActions.receiveAsFlow()

    protected fun emitAction(action: Action) {
        viewModelScope.launch {
            _viewActions.send(action)
        }
    }

    /**
     * Handle User Intent/Events
     */
    abstract fun handleEvent(event: Event)
}