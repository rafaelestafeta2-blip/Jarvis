package com.example.jarvis.core.mcp

import android.content.Context

interface McpTool {
    val name: String
    val description: String
    suspend fun execute(
        context: Context,
        requestId: String,
        arguments: Map<String, Any>
    ): McpToolResult
}
