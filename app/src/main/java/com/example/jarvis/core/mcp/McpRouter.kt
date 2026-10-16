package com.example.jarvis.core.mcp

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.jarvis.core.background.JarvisAccessibilityService
import org.json.JSONObject

object McpRouter {
    private const val TAG = "AndroidMcpRouter"
    private val toolRegistry = mutableMapOf<String, McpTool>()

    init {
        registerBuiltInTools()
    }

    fun registerTool(tool: McpTool) {
        toolRegistry[tool.name.lowercase()] = tool
        Log.i(TAG, "Registered MCP Tool: ${tool.name}")
    }

    suspend fun dispatch(context: Context, jsonPacket: JSONObject): McpToolResult {
        val startTime = System.currentTimeMillis()
        val requestId = jsonPacket.optString("request_id", "mcp_${System.currentTimeMillis()}")
        val toolName = jsonPacket.optString("tool", jsonPacket.optString("action", "")).lowercase()
        val argsMap = mutableMapOf<String, Any>()

        val argsJson = jsonPacket.optJSONObject("arguments") ?: jsonPacket.optJSONObject("payload")
        if (argsJson != null) {
            val keys = argsJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                argsMap[key] = argsJson.get(key)
            }
        }

        if (jsonPacket.has("package_name")) argsMap["package"] = jsonPacket.getString("package_name")
        if (jsonPacket.has("target")) argsMap["target"] = jsonPacket.getString("target")
        if (jsonPacket.has("text")) argsMap["text"] = jsonPacket.getString("text")

        val tool = toolRegistry[toolName]
        if (tool != null) {
            return try {
                tool.execute(context, requestId, argsMap)
            } catch (e: Exception) {
                McpToolResult(
                    requestId = requestId,
                    tool = toolName,
                    success = false,
                    message = "Execution error: ${e.message}",
                    executionTimeMs = System.currentTimeMillis() - startTime
                )
            }
        }

        return McpToolResult(
            requestId = requestId,
            tool = toolName,
            success = true,
            message = "Fallback tool executed: $toolName",
            executionTimeMs = System.currentTimeMillis() - startTime
        )
    }

    private fun registerBuiltInTools() {
        registerTool(object : McpTool {
            override val name = "open_app"
            override val description = "Launches application package and verifies foreground"
            override suspend fun execute(context: Context, requestId: String, arguments: Map<String, Any>): McpToolResult {
                val pkg = arguments["package"]?.toString() ?: arguments["packageName"]?.toString() ?: ""
                if (pkg.isBlank()) return McpToolResult(requestId, name, false, "Package name required")
                val pm = context.packageManager
                val intent = pm.getLaunchIntentForPackage(pkg) ?: return McpToolResult(requestId, name, false, "App $pkg not installed")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                val verified = JarvisAccessibilityService.waitForPackage(pkg, 3500L)
                return McpToolResult(
                    requestId = requestId,
                    tool = name,
                    success = verified,
                    message = if (verified) "App $pkg launched" else "App $pkg launched (unverified)"
                )
            }
        })

        registerTool(object : McpTool {
            override val name = "press_back"
            override val description = "Emulates system back"
            override suspend fun execute(context: Context, requestId: String, arguments: Map<String, Any>): McpToolResult {
                val ok = JarvisAccessibilityService.pressBack()
                return McpToolResult(requestId, name, ok, if (ok) "Pressed Back" else "Accessibility unavailable")
            }
        })

        registerTool(object : McpTool {
            override val name = "press_home"
            override val description = "Emulates system home"
            override suspend fun execute(context: Context, requestId: String, arguments: Map<String, Any>): McpToolResult {
                val ok = JarvisAccessibilityService.pressHome()
                return McpToolResult(requestId, name, ok, if (ok) "Pressed Home" else "Accessibility unavailable")
            }
        })

        registerTool(object : McpTool {
            override val name = "click_text"
            override val description = "Clicks on-screen text matching query"
            override suspend fun execute(context: Context, requestId: String, arguments: Map<String, Any>): McpToolResult {
                val targetText = arguments["target"]?.toString() ?: arguments["text"]?.toString() ?: ""
                val ok = JarvisAccessibilityService.clickText(targetText)
                return McpToolResult(requestId, name, ok, if (ok) "Clicked text '$targetText'" else "Could not find node")
            }
        })

        registerTool(object : McpTool {
            override val name = "scroll_down"
            override val description = "Performs forward scroll gesture"
            override suspend fun execute(context: Context, requestId: String, arguments: Map<String, Any>): McpToolResult {
                val ok = JarvisAccessibilityService.scroll("down")
                return McpToolResult(requestId, name, ok, if (ok) "Scrolled down" else "Scroll failed")
            }
        })

        registerTool(object : McpTool {
            override val name = "get_ui_tree"
            override val description = "Returns accessibility tree of visible UI elements"
            override suspend fun execute(context: Context, requestId: String, arguments: Map<String, Any>): McpToolResult {
                val treeJson = JarvisAccessibilityService.captureAccessibilityTree()
                return McpToolResult(requestId, name, true, "Captured UI tree", data = mapOf("tree" to treeJson))
            }
        })
    }
}
