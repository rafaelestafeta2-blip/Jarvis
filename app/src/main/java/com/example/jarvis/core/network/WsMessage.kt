package com.example.jarvis.core.network

sealed class WsMessage {
    data class Connected(val welcomeText: String) : WsMessage()
    data class StreamStart(val text: String) : WsMessage()
    data class StreamChunk(val text: String, val done: Boolean = false) : WsMessage()
    data class StreamEnd(val text: String, val done: Boolean = true) : WsMessage()
    data class Plan(val goal: String, val stepsCount: Int) : WsMessage()
    data class ToolRequest(
        val requestId: String,
        val action: String,
        val packageName: String = "",
        val appName: String = "",
        val target: String = ""
    ) : WsMessage()
    data class ToolCall(
        val action: String,
        val packageName: String = "",
        val appName: String = "",
        val target: String = ""
    ) : WsMessage()
    data class TaskProgress(
        val taskName: String,
        val status: String,
        val detail: String = "",
        val agent: String = "",
        val tool: String = ""
    ) : WsMessage()
    data object Heartbeat : WsMessage()
    data class Error(val message: String) : WsMessage()
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}
