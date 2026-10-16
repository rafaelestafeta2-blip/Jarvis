package com.example.jarvis.feature.assistant.model

data class AssistantUiState(
    val isOverlayVisible: Boolean = false,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isTyping: Boolean = false,
    val voiceTranscript: String = "",
    val currentStreamingText: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isConnectedToBackend: Boolean = false,
    val statusMessage: String = "Ready"
)
