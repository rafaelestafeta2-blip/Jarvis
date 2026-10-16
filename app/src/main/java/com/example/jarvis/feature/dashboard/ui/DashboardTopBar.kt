package com.example.jarvis.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.core.ui.components.PulseOrbVisualizer
import com.example.jarvis.core.ui.theme.ElectricViolet
import com.example.jarvis.core.ui.theme.GlassBorder
import com.example.jarvis.core.ui.theme.GlassDark
import com.example.jarvis.core.ui.theme.NeonCyan
import com.example.jarvis.core.ui.theme.TextMuted
import com.example.jarvis.core.ui.theme.TextPrimary

@Composable
fun DashboardTopBar(
    isEngineRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(GlassDark, ElectricViolet.copy(alpha = 0.25f))))
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "JARVIS OS",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonCyan,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Grounded Agentic AI Assistant",
                fontSize = 12.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isEngineRunning) "Core System Active • 24/7 Voice Ready" else "Standby Mode • Tap Start to Launch",
                fontSize = 11.sp,
                color = if (isEngineRunning) NeonCyan else TextMuted
            )
        }

        PulseOrbVisualizer(
            isListening = isEngineRunning,
            isSpeaking = false,
            size = 54.dp
        )
    }
}
