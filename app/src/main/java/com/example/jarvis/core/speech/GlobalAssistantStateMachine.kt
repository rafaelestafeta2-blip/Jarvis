package com.example.jarvis.core.speech

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object GlobalAssistantStateMachine {
    private const val TAG = "GlobalAssistantState"

    enum class AssistantState {
        DISABLED,
        WAKELISTENING,
        WAKEDETECTED,
        OPENINGOVERLAY,
        LISTENING,
        PROCESSING,
        EXECUTINGACTION,
        SPEAKING,
        CLOSING
    }

    private val _stateFlow = MutableStateFlow(AssistantState.WAKELISTENING)
    val stateFlow: StateFlow<AssistantState> = _stateFlow.asStateFlow()
    private val mutex = Mutex()

    fun currentState(): AssistantState = _stateFlow.value

    suspend fun transitionTo(newState: AssistantState): Boolean = mutex.withLock {
        val oldState = _stateFlow.value
        if (oldState == newState) return true
        Log.i(TAG, "GLOBAL STATE TRANSITION: [$oldState] -> [$newState]")
        _stateFlow.value = newState
        return true
    }

    fun transitionToSync(newState: AssistantState): Boolean {
        val oldState = _stateFlow.value
        if (oldState == newState) return true
        Log.i(TAG, "GLOBAL STATE TRANSITION (SYNC): [$oldState] -> [$newState]")
        _stateFlow.value = newState
        return true
    }

    suspend fun resetToWakeListening(reason: String = "complete") = mutex.withLock {
        Log.i(TAG, "Resetting Global State to WAKELISTENING ($reason)")
        _stateFlow.value = AssistantState.WAKELISTENING
    }
}
