package com.example.jarvis.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.core.ui.components.GlassmorphicCard
import com.example.jarvis.core.ui.components.PermissionRow
import com.example.jarvis.core.ui.theme.GlassBorderMuted
import com.example.jarvis.core.ui.theme.NeonCyan
import com.example.jarvis.core.ui.theme.TextPrimary

@Composable
fun PermissionsCard(
    hasMicPermission: Boolean,
    hasOverlayPermission: Boolean,
    hasPhonePermission: Boolean,
    hasSmsPermission: Boolean,
    hasCameraPermission: Boolean,
    isBatteryExempt: Boolean,
    isAccessibilityEnabled: Boolean,
    isNotificationListenerEnabled: Boolean,
    canWriteSettings: Boolean,
    onGrantMic: () -> Unit,
    onGrantOverlay: () -> Unit,
    onGrantPhone: () -> Unit,
    onGrantSms: () -> Unit,
    onGrantCamera: () -> Unit,
    onGrantBattery: () -> Unit,
    onGrantAccessibility: () -> Unit,
    onGrantNotificationListener: () -> Unit,
    onGrantWriteSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Permissões do Sistema & Agente",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Restricted Settings Guide Banner (Android 13+)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "🛡️ Como liberar Acessibilidade e Notificações (Android 13+):\nVá em Configurações > Aplicativos > JARVIS OS > toque nos 3 pontinhos (⋮) no topo e selecione 'Permitir definições restritas'.",
                    color = Color.White,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            HorizontalDivider(color = GlassBorderMuted)

            PermissionRow(
                title = "🎤 Microfone",
                description = "Reconhecimento de voz e chamada 'Jarvis'",
                isGranted = hasMicPermission,
                testTag = "grant_mic_perm",
                onRequest = onGrantMic
            )
            PermissionRow(
                title = "🖼️ Sobrepor a outros apps",
                description = "Janela flutuante do assistente por cima dos apps",
                isGranted = hasOverlayPermission,
                testTag = "grant_overlay_perm",
                onRequest = onGrantOverlay
            )
            PermissionRow(
                title = "📞 Telefone & Contatos",
                description = "Fazer chamadas e discagem rápida",
                isGranted = hasPhonePermission,
                testTag = "grant_phone_perm",
                onRequest = onGrantPhone
            )
            PermissionRow(
                title = "💬 Mensagens SMS (Opcional)",
                description = "Envio de SMS mãos-livres",
                isGranted = hasSmsPermission,
                testTag = "grant_sms_perm",
                onRequest = onGrantSms
            )
            PermissionRow(
                title = "📸 Câmera",
                description = "Abertura rápida e fotos",
                isGranted = hasCameraPermission,
                testTag = "grant_camera_perm",
                onRequest = onGrantCamera
            )
            PermissionRow(
                title = "⚡ Isenção de Bateria",
                description = "Impede o Android de matar o serviço 24/7",
                isGranted = isBatteryExempt,
                testTag = "grant_battery_perm",
                onRequest = onGrantBattery
            )
            PermissionRow(
                title = "🤖 Serviço de Acessibilidade",
                description = "Controle de tela e cliques autônomos",
                isGranted = isAccessibilityEnabled,
                testTag = "grant_accessibility_perm",
                onRequest = onGrantAccessibility
            )
            PermissionRow(
                title = "🔔 Acesso a Notificações",
                description = "Ler mensagens do WhatsApp e chamadas",
                isGranted = isNotificationListenerEnabled,
                testTag = "grant_notification_perm",
                onRequest = onGrantNotificationListener
            )
            PermissionRow(
                title = "⚙️ Escrever Configurações",
                description = "Ajustar volume e parâmetros do sistema",
                isGranted = canWriteSettings,
                testTag = "grant_write_settings_perm",
                onRequest = onGrantWriteSettings
            )
        }
    }
}
