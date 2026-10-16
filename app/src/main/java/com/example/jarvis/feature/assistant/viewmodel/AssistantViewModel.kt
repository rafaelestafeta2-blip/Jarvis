package com.example.jarvis.feature.assistant.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jarvis.core.network.ConnectionState
import com.example.jarvis.core.network.JarvisWebSocketClient
import com.example.jarvis.core.network.WsMessage
import com.example.jarvis.core.speech.GlobalAssistantStateMachine
import com.example.jarvis.core.speech.MicrophoneStateMachine
import com.example.jarvis.feature.assistant.engine.JarvisCommandExecutor
import com.example.jarvis.feature.assistant.engine.JarvisTextToSpeech
import com.example.jarvis.feature.assistant.engine.VoiceInputManager
import com.example.jarvis.feature.assistant.model.AssistantUiState
import com.example.jarvis.feature.assistant.model.ChatMessage
import com.example.jarvis.feature.assistant.model.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssistantViewModel(
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AssistantUiState(
            statusMessage = "Pronto, senhor",
            messages = listOf(
                ChatMessage(
                    role = MessageRole.ASSISTANT,
                    text = "JARVIS OS online. Pronto para comandos de voz e controle do sistema, senhor."
                )
            )
        )
    )
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private val commandExecutor = JarvisCommandExecutor(context)
    private val wsClient = JarvisWebSocketClient(scope = viewModelScope)
    private val tts = JarvisTextToSpeech(context) { isSpeaking ->
        _uiState.value = _uiState.value.copy(isSpeaking = isSpeaking)
    }

    private var voiceInputManager: VoiceInputManager? = null

    init {
        initVoiceInput()
        observeWsConnection()
        observeWsMessages()
        wsClient.connect()
    }

    private fun initVoiceInput() {
        voiceInputManager = VoiceInputManager(
            context = context,
            onPartialTranscript = { partial ->
                _uiState.value = _uiState.value.copy(voiceTranscript = partial)
            },
            onFinalTranscript = { finalQuery ->
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    voiceTranscript = finalQuery
                )
                submitQuery(finalQuery)
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    statusMessage = error
                )
            }
        )
    }

    private fun observeWsConnection() {
        viewModelScope.launch {
            wsClient.connectionState.collect { state ->
                val connected = state == ConnectionState.CONNECTED
                _uiState.value = _uiState.value.copy(
                    isConnectedToBackend = connected,
                    statusMessage = if (connected) "Connected to JARVIS Engine" else "Local Autonomous Engine Ready"
                )
            }
        }
    }

    private fun observeWsMessages() {
        viewModelScope.launch {
            wsClient.incomingMessages().collect { msg ->
                when (msg) {
                    is WsMessage.StreamChunk -> {
                        _uiState.value = _uiState.value.copy(
                            currentStreamingText = _uiState.value.currentStreamingText + msg.text,
                            isTyping = !msg.done
                        )
                        if (msg.done) finalizeStreaming()
                    }
                    is WsMessage.StreamEnd -> finalizeStreaming()
                    is WsMessage.ToolCall -> handleToolCall(msg.action)
                    is WsMessage.Error -> {
                        addMessage(MessageRole.ASSISTANT, "Error: ${msg.message}")
                        _uiState.value = _uiState.value.copy(isTyping = false)
                    }
                    else -> Unit
                }
            }
        }
    }

    fun submitQuery(query: String) {
        if (query.isBlank()) return
        addMessage(MessageRole.USER, query)
        _uiState.value = _uiState.value.copy(voiceTranscript = "", isTyping = true)

        if (wsClient.isConnected()) {
            wsClient.sendText(query)
        } else {
            // Local autonomous execution fallback
            viewModelScope.launch {
                val response = commandExecutor.executeLocalCommand(query)
                _uiState.value = _uiState.value.copy(isTyping = false)
                addMessage(MessageRole.ASSISTANT, response)
                tts.speak(response)
            }
        }
    }

    private fun handleToolCall(action: String) {
        val result = commandExecutor.executeLocalCommand(action)
        addMessage(MessageRole.ASSISTANT, result)
        tts.speak(result)
    }

    private fun finalizeStreaming() {
        val streamed = _uiState.value.currentStreamingText
        if (streamed.isNotBlank()) {
            addMessage(MessageRole.ASSISTANT, streamed)
            tts.speak(streamed)
        }
        _uiState.value = _uiState.value.copy(currentStreamingText = "", isTyping = false)
    }

    fun toggleListening() {
        if (_uiState.value.isListening) {
            voiceInputManager?.stopListening()
            _uiState.value = _uiState.value.copy(isListening = false)
        } else {
            tts.stop()
            _uiState.value = _uiState.value.copy(isListening = true, statusMessage = "Listening...")
            voiceInputManager?.startListening()
        }
    }

    fun stopSpeaking() {
        tts.stop()
    }

    private fun addMessage(role: MessageRole, text: String) {
        val msg = ChatMessage(role = role, text = text)
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + msg)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(messages = emptyList())
    }

    override fun onCleared() {
        voiceInputManager?.destroy()
        tts.shutdown()
        wsClient.disconnect()
        super.onCleared()
    }
}
