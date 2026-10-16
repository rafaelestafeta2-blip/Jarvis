package com.example.jarvis.core.network

object NetworkConfig {
    const val DEFAULT_SERVER_HOST = "10.0.2.2"
    const val DEFAULT_SERVER_PORT = 8000
    const val DEFAULT_WS_PATH = "/ws"

    fun getWebSocketUrl(host: String = DEFAULT_SERVER_HOST, port: Int = DEFAULT_SERVER_PORT): String {
        return "ws://$host:$port$DEFAULT_WS_PATH"
    }

    const val HEARTBEAT_INTERVAL_MS = 15000L
    const val RECONNECT_DELAY_MS = 3000L
    const val MAX_RECONNECT_ATTEMPTS = 5
}
