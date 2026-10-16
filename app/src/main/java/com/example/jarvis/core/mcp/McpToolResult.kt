package com.example.jarvis.core.mcp

data class McpToolResult(
    val requestId: String,
    val tool: String,
    val success: Boolean,
    val message: String,
    val retryable: Boolean = false,
    val executionTimeMs: Long = 0,
    val data: Map<String, Any>? = null
)
