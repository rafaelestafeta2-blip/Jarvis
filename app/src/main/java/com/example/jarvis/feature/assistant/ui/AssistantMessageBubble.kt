package com.example.jarvis.feature.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.core.ui.theme.DarkSurface
import com.example.jarvis.core.ui.theme.ElectricViolet
import com.example.jarvis.core.ui.theme.GlassBorder
import com.example.jarvis.core.ui.theme.NeonPurple
import com.example.jarvis.core.ui.theme.TextPrimary
import com.example.jarvis.feature.assistant.model.ChatMessage
import com.example.jarvis.feature.assistant.model.MessageRole

@Composable
fun AssistantMessageBubble(
    msg: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = msg.role == MessageRole.USER
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (isUser) ElectricViolet.copy(alpha = 0.85f) else DarkSurface.copy(alpha = 0.9f))
                .border(1.dp, if (isUser) NeonPurple.copy(alpha = 0.4f) else GlassBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = msg.text,
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
