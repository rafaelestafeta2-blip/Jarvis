package com.example.jarvis.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.core.ui.components.GlassmorphicCard
import com.example.jarvis.core.ui.components.StatusBadge
import com.example.jarvis.core.ui.theme.ElectricViolet
import com.example.jarvis.core.ui.theme.GlassBorderMuted
import com.example.jarvis.core.ui.theme.GlassWhite
import com.example.jarvis.core.ui.theme.NeonCyan
import com.example.jarvis.core.ui.theme.TextMuted
import com.example.jarvis.core.ui.theme.TextPrimary

@Composable
fun EngineControlCard(
    isWakeWordRunning: Boolean,
    isOverlayRunning: Boolean,
    onToggleWakeWord: () -> Unit,
    onToggleOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Engine Core State",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            HorizontalDivider(color = GlassBorderMuted)

            // Wake Word Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Wake Word Engine", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("AudioRecord detector for 'Jarvis'", fontSize = 11.sp, color = TextMuted)
                }
                StatusBadge(active = isWakeWordRunning)
            }

            Button(
                onClick = onToggleWakeWord,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWakeWordRunning) GlassWhite else ElectricViolet,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("toggle_wake_word_button")
            ) {
                Text(
                    text = if (isWakeWordRunning) "Stop Wake Word Engine" else "Start Wake Word Engine",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Overlay Window Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Assistant Overlay Window", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("System Alert Window over other apps", fontSize = 11.sp, color = TextMuted)
                }
                StatusBadge(active = isOverlayRunning)
            }

            Button(
                onClick = onToggleOverlay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOverlayRunning) GlassWhite else NeonCyan,
                    contentColor = if (isOverlayRunning) TextPrimary else Color(0xFF070B14)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("toggle_overlay_button")
            ) {
                Text(
                    text = if (isOverlayRunning) "Close Assistant Overlay" else "Launch Assistant Overlay",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
