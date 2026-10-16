package com.example.jarvis.core.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class JarvisWebSocketClient(
    private val serverUrl: String = NetworkConfig.getWebSocketUrl(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val tag = "JarvisWSClient"
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val incomingFlow = MutableSharedFlow<WsMessage>(extraBufferCapacity = 64)
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var heartbeatJob: Job? = null
    private var isExplicitDisconnect = false

    fun incomingMessages(): Flow<WsMessage> = incomingFlow.asSharedFlow()
    fun isConnected(): Boolean = _connectionState.value == ConnectionState.CONNECTED

    fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTED) return
        isExplicitDisconnect = false
        _connectionState.value = ConnectionState.CONNECTING
        Log.i(tag, "Connecting to WebSocket at $serverUrl")

        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, createListener())
    }

    private fun createListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _connectionState.value = ConnectionState.CONNECTED
            Log.i(tag, "WebSocket connected successfully.")
            startHeartbeat()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            parseIncoming(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
            _connectionState.value = ConnectionState.DISCONNECTED
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.w(tag, "WebSocket connection failed: ${t.message}")
            _connectionState.value = ConnectionState.DISCONNECTED
            stopHeartbeat()
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive && isConnected()) {
                delay(NetworkConfig.HEARTBEAT_INTERVAL_MS)
                sendHeartbeat()
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    fun disconnect() {
        isExplicitDisconnect = true
        stopHeartbeat()
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun sendText(text: String, contextJson: JSONObject? = null) {
        val payload = JSONObject().apply { put("text", text) }
        val envelope = JSONObject().apply {
            put("type", "message")
            put("payload", payload)
            if (contextJson != null) put("context_state", contextJson)
        }
        webSocket?.send(envelope.toString())
    }

    fun sendHeartbeat() {
        val envelope = JSONObject().apply {
            put("type", "ping")
            put("payload", JSONObject())
        }
        webSocket?.send(envelope.toString())
    }

    fun sendToolResult(reqId: String, success: Boolean, action: String, message: String, timeMs: Long) {
        val payload = JSONObject().apply {
            put("request_id", reqId)
            put("success", success)
            put("action", action)
            put("message", message)
            put("execution_time_ms", timeMs)
        }
        val envelope = JSONObject().apply {
            put("type", "tool_result")
            put("payload", payload)
        }
        webSocket?.send(envelope.toString())
    }

    private fun parseIncoming(raw: String) {
        try {
            val json = JSONObject(raw)
            val type = json.optString("type", "").lowercase()
            val payload = json.optJSONObject("payload")

            val msg: WsMessage? = when (type) {
                "connected" -> WsMessage.Connected(payload?.optString("text", "Connected") ?: "Connected")
                "stream_start" -> WsMessage.StreamStart(payload?.optString("text", "") ?: "")
                "stream_chunk", "token", "response_chunk" -> WsMessage.StreamChunk(
                    text = payload?.optString("text", "") ?: "",
                    done = payload?.optBoolean("done", false) ?: false
                )
                "stream_end", "completed" -> WsMessage.StreamEnd(payload?.optString("text", "") ?: "", true)
                "plan" -> WsMessage.Plan(
                    goal = payload?.optString("goal", "") ?: "",
                    stepsCount = payload?.optJSONArray("steps")?.length() ?: 0
                )
                "tool_request" -> WsMessage.ToolRequest(
                    requestId = payload?.optString("request_id", "") ?: "",
                    action = payload?.optString("action", "") ?: "",
                    packageName = payload?.optString("package_name", "") ?: "",
                    appName = payload?.optString("app_name", "") ?: "",
                    target = payload?.optString("target", "") ?: ""
                )
                "tool_call", "intent_detected" -> {
                    val intent = payload?.optJSONObject("intent") ?: payload
                    WsMessage.ToolCall(
                        action = intent?.optString("action", "") ?: "",
                        packageName = intent?.optString("package_name", "") ?: "",
                        appName = intent?.optString("app_name", "") ?: "",
                        target = intent?.optString("target", "") ?: ""
                    )
                }
                "task_progress", "task_start", "task_finish" -> WsMessage.TaskProgress(
                    taskName = payload?.optString("task_name", type) ?: type,
                    status = payload?.optString("status", type) ?: type,
                    detail = payload?.optString("detail", "") ?: ""
                )
                "pong", "heartbeat" -> WsMessage.Heartbeat
                "error" -> WsMessage.Error(payload?.optString("message", "Error") ?: "Error")
                else -> null
            }
            if (msg != null) incomingFlow.tryEmit(msg)
        } catch (e: Exception) {
            Log.e(tag, "JSON parse error", e)
        }
    }
}
