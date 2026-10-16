package com.example.jarvis.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.core.telemetry.RoadHazardAlert
import com.example.jarvis.core.ui.components.GlassmorphicCard
import com.example.jarvis.core.ui.components.StatusBadge
import com.example.jarvis.core.ui.theme.DarkSurfaceVariant
import com.example.jarvis.core.ui.theme.ElectricViolet
import com.example.jarvis.core.ui.theme.GlassBorderMuted
import com.example.jarvis.core.ui.theme.NeonCyan
import com.example.jarvis.core.ui.theme.StatusWarning
import com.example.jarvis.core.ui.theme.TextMuted
import com.example.jarvis.core.ui.theme.TextPrimary

@Composable
fun GpsCopilotCard(
    isMonitoring: Boolean,
    currentSpeedKmH: Int,
    lastAlert: RoadHazardAlert?,
    onToggleMonitoring: (Boolean) -> Unit,
    onScanRoadAhead: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricViolet.copy(alpha = 0.3f))
                            .border(1.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = "GPS", tint = NeonCyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Copiloto Sentinela GPS", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Radar autônomo de blitz, acidentes e radares", fontSize = 11.sp, color = TextMuted)
                    }
                }
                StatusBadge(active = isMonitoring, activeText = "MONITORANDO", inactiveText = "STANDBY")
            }

            HorizontalDivider(color = GlassBorderMuted)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Velocidade Telemetria", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = "$currentSpeedKmH km/h",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }

                Switch(
                    checked = isMonitoring,
                    onCheckedChange = onToggleMonitoring,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier.testTag("gps_copilot_switch")
                )
            }

            if (lastAlert != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusWarning.copy(alpha = 0.15f))
                        .border(1.dp, StatusWarning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Alerta", tint = StatusWarning, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("${lastAlert.title} a ${lastAlert.distanceMeters}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Via monitorada • Limite ${lastAlert.speedLimitKmH} km/h", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }

            Button(
                onClick = onScanRoadAhead,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = TextPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("scan_road_ahead_button")
            ) {
                Text("Escanear Ocorrências à Frente por Voz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
