package com.example.jarvis.feature.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.core.ui.theme.ElectricViolet
import com.example.jarvis.core.ui.theme.GlassWhite
import com.example.jarvis.core.ui.theme.NeonCyan
import com.example.jarvis.core.ui.theme.NeonPurple
import com.example.jarvis.core.ui.theme.TextMuted
import com.example.jarvis.core.ui.theme.TextPrimary

@Composable
fun AssistantInputBar(
    textInput: String,
    isListening: Boolean,
    onTextInputChange: (String) -> Unit,
    onSendQuery: (String) -> Unit,
    onToggleListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = textInput,
            onValueChange = onTextInputChange,
            placeholder = { Text("Ask JARVIS or give a phone command...", fontSize = 13.sp, color = TextMuted) },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("assistant_query_input"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GlassWhite,
                unfocusedContainerColor = GlassWhite,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (textInput.isNotBlank()) {
                    onSendQuery(textInput)
                }
            })
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onToggleListening,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isListening) NeonCyan else ElectricViolet)
                .testTag("mic_toggle_button")
        ) {
            Icon(
                if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Microphone",
                tint = Color.White
            )
        }

        if (textInput.isNotBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = { onSendQuery(textInput) },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(NeonPurple)
                    .testTag("send_query_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}
