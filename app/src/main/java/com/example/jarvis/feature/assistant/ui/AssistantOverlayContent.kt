package com.example.jarvis.feature.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.core.ui.components.PulseOrbVisualizer
import com.example.jarvis.core.ui.theme.DarkSurface
import com.example.jarvis.core.ui.theme.GlassBorder
import com.example.jarvis.core.ui.theme.GlassDark
import com.example.jarvis.core.ui.theme.GlassWhite
import com.example.jarvis.core.ui.theme.NeonCyan
import com.example.jarvis.core.ui.theme.TextMuted
import com.example.jarvis.feature.assistant.viewmodel.AssistantViewModel

@Composable
fun AssistantOverlayContent(
    viewModel: AssistantViewModel,
    onDismiss: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(uiState.voiceTranscript) {
        if (uiState.voiceTranscript.isNotEmpty()) {
            textInput = uiState.voiceTranscript
        }
    }

    val quickActions = remember {
        listOf("📱 Abrir WhatsApp", "📸 Tirar Foto", "⏰ Definir Alarme", "📱 Abrir Instagram", "🗺️ Navegar para Casa", "💬 Quem é você?")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(GlassDark, DarkSurface)))
                .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulseOrbVisualizer(
                        isListening = uiState.isListening,
                        isSpeaking = uiState.isSpeaking,
                        size = 36.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("JARVIS OS", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(uiState.statusMessage, color = TextMuted, fontSize = 11.sp)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp).testTag("close_overlay_button")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Messages Container
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassWhite)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { msg ->
                    AssistantMessageBubble(msg)
                }
                if (uiState.isTyping) {
                    item {
                        Text("● ● ●", color = NeonCyan, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Prompt Chips
            AssistantQuickChips(
                quickActions = quickActions,
                onActionSelected = { cmd -> viewModel.submitQuery(cmd) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Input Row
            AssistantInputBar(
                textInput = textInput,
                isListening = uiState.isListening,
                onTextInputChange = { textInput = it },
                onSendQuery = { query ->
                    viewModel.submitQuery(query)
                    textInput = ""
                },
                onToggleListening = { viewModel.toggleListening() }
            )
        }
    }
}
