package com.example.jarvis.feature.assistant.model

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString() + "_" + (1000..9999).random(),
    val role: MessageRole,
    val text: String,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
