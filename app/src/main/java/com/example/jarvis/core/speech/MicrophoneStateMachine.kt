package com.example.jarvis.core.speech

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object MicrophoneStateMachine {
    private const val TAG = "MicStateMachine"

    enum class State {
        DISABLED,
        INITIALIZING,
        WAKELISTENING,
        WAKEDETECTED,
        OPENINGOVERLAY,
        LISTENING,
        THINKING,
        SPEAKING,
        CLOSING
    }

    enum class Owner {
        NONE,
        WAKE_WORD_ENGINE,
        SPEECH_RECOGNIZER
    }

    private val _stateFlow = MutableStateFlow(State.DISABLED)
    val stateFlow: StateFlow<State> = _stateFlow.asStateFlow()

    private val _ownerFlow = MutableStateFlow(Owner.NONE)
    val ownerFlow: StateFlow<Owner> = _ownerFlow.asStateFlow()

    private val mutex = Mutex()

    fun currentState(): State = _stateFlow.value
    fun currentOwner(): Owner = _ownerFlow.value

    suspend fun transitionTo(newState: State, newOwner: Owner = _ownerFlow.value): Boolean = mutex.withLock {
        Log.i(TAG, "Transition: [${_stateFlow.value}|${_ownerFlow.value}] -> [$newState|$newOwner]")
        _stateFlow.value = newState
        _ownerFlow.value = newOwner
        true
    }

    suspend fun wakeWordEngineAcquire(): Boolean = mutex.withLock {
        if (_ownerFlow.value == Owner.NONE || _ownerFlow.value == Owner.WAKE_WORD_ENGINE) {
            _stateFlow.value = State.WAKELISTENING
            _ownerFlow.value = Owner.WAKE_WORD_ENGINE
            Log.i(TAG, "WakeWord Engine acquired microphone")
            return true
        }
        false
    }

    suspend fun speechRecognizerAcquire(): Boolean = mutex.withLock {
        _stateFlow.value = State.LISTENING
        _ownerFlow.value = Owner.SPEECH_RECOGNIZER
        Log.i(TAG, "SpeechRecognizer acquired microphone")
        true
    }

    suspend fun speechRecognizerRelease(): Boolean = mutex.withLock {
        _stateFlow.value = State.WAKELISTENING
        _ownerFlow.value = Owner.WAKE_WORD_ENGINE
        Log.i(TAG, "Microphone released to WakeWord Engine")
        true
    }

    suspend fun forceReset(reason: String) = mutex.withLock {
        Log.w(TAG, "forceReset called: $reason")
        _stateFlow.value = State.WAKELISTENING
        _ownerFlow.value = Owner.WAKE_WORD_ENGINE
    }
}
