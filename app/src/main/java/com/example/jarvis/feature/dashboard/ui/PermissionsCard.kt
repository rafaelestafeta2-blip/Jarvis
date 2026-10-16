package com.example.jarvis.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.core.ui.components.GlassmorphicCard
import com.example.jarvis.core.ui.components.PermissionRow
import com.example.jarvis.core.ui.theme.GlassBorderMuted
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
                text = "System & Agent Permissions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            HorizontalDivider(color = GlassBorderMuted)

            PermissionRow(
                title = "🎤 Microphone",
                description = "Voice recognition & 'Jarvis' wake-word",
                isGranted = hasMicPermission,
                testTag = "grant_mic_perm",
                onRequest = onGrantMic
            )
            PermissionRow(
                title = "🖼️ Display Over Apps",
                description = "Floating assistant bottom overlay window",
                isGranted = hasOverlayPermission,
                testTag = "grant_overlay_perm",
                onRequest = onGrantOverlay
            )
            PermissionRow(
                title = "📞 Phone & Contacts",
                description = "Direct voice calling & contact lookup",
                isGranted = hasPhonePermission,
                testTag = "grant_phone_perm",
                onRequest = onGrantPhone
            )
            PermissionRow(
                title = "💬 SMS Messaging",
                description = "Voice text messaging & hands-free alerts",
                isGranted = hasSmsPermission,
                testTag = "grant_sms_perm",
                onRequest = onGrantSms
            )
            PermissionRow(
                title = "📸 Camera Access",
                description = "Hands-free selfies & camera launch",
                isGranted = hasCameraPermission,
                testTag = "grant_camera_perm",
                onRequest = onGrantCamera
            )
            PermissionRow(
                title = "⚡ Battery Exemption",
                description = "Prevents Android from killing 24/7 service",
                isGranted = isBatteryExempt,
                testTag = "grant_battery_perm",
                onRequest = onGrantBattery
            )
            PermissionRow(
                title = "🤖 Accessibility Service",
                description = "Agentic UI automation & screen control",
                isGranted = isAccessibilityEnabled,
                testTag = "grant_accessibility_perm",
                onRequest = onGrantAccessibility
            )
            PermissionRow(
                title = "🔔 Notification Access",
                description = "Read notifications & WhatsApp messages",
                isGranted = isNotificationListenerEnabled,
                testTag = "grant_notification_perm",
                onRequest = onGrantNotificationListener
            )
            PermissionRow(
                title = "⚙️ System Write Settings",
                description = "Adjust brightness & sound volume levels",
                isGranted = canWriteSettings,
                testTag = "grant_write_settings_perm",
                onRequest = onGrantWriteSettings
            )
        }
    }
}
