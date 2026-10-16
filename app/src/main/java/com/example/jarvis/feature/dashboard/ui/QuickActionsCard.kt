package com.example.jarvis.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.core.ui.components.GlassmorphicCard
import com.example.jarvis.core.ui.theme.DarkSurfaceVariant
import com.example.jarvis.core.ui.theme.GlassBorderMuted
import com.example.jarvis.core.ui.theme.TextMuted
import com.example.jarvis.core.ui.theme.TextPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickActionsCard(
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleActions = listOf(
        "☀️ Briefing Matinal",
        "Tocar Música",
        "Me acorde às 7h",
        "Minhas Datas Importantes",
        "Verificar Saldo",
        "Trace uma rota para a farmácia",
        "Abrir WhatsApp",
        "Tirar Foto"
    )

    GlassmorphicCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Ações do Agente Autônomo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Toque para testar comandos de voz, rotinas e ferramentas MCP.",
                fontSize = 12.sp,
                color = TextMuted
            )

            HorizontalDivider(color = GlassBorderMuted)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sampleActions.forEach { action ->
                    Button(
                        onClick = { onActionClick(action) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("quick_action_${action.replace(' ', '_').lowercase()}")
                    ) {
                        Text(action, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
