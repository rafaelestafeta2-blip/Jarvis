package com.example.jarvis.feature.dashboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.core.memory.UserProfile
import com.example.jarvis.core.telemetry.RoadHazardAlert
import com.example.jarvis.core.ui.theme.ElectricViolet
import com.example.jarvis.core.ui.theme.NeonCyan
import com.example.jarvis.core.ui.theme.ObsidianBlack
import com.example.jarvis.feature.assistant.ui.AssistantOverlayContent
import com.example.jarvis.feature.assistant.viewmodel.AssistantViewModel

@Composable
fun DashboardScreen(
    assistantViewModel: AssistantViewModel,
    profile: UserProfile,
    isWakeWordRunning: Boolean,
    isOverlayRunning: Boolean,
    isGpsMonitoring: Boolean,
    currentSpeedKmH: Int,
    lastAlert: RoadHazardAlert?,
    hasMicPermission: Boolean,
    hasOverlayPermission: Boolean,
    hasPhonePermission: Boolean,
    hasSmsPermission: Boolean,
    hasCameraPermission: Boolean,
    isBatteryExempt: Boolean,
    isAccessibilityEnabled: Boolean,
    isNotificationListenerEnabled: Boolean,
    canWriteSettings: Boolean,
    onGrantAllPermissions: () -> Unit,
    onToggleWakeWord: () -> Unit,
    onToggleOverlay: () -> Unit,
    onToggleGpsMonitoring: (Boolean) -> Unit,
    onScanRoadAhead: () -> Unit,
    onToggleMasterLock: (Boolean) -> Unit,
    onVerifyBiometrics: () -> Unit,
    onGrantMic: () -> Unit,
    onGrantOverlay: () -> Unit,
    onGrantPhone: () -> Unit,
    onGrantSms: () -> Unit,
    onGrantCamera: () -> Unit,
    onGrantBattery: () -> Unit,
    onGrantAccessibility: () -> Unit,
    onGrantNotificationListener: () -> Unit,
    onGrantWriteSettings: () -> Unit
) {
    var showEmbeddedAssistant by remember { mutableStateOf(false) }

    Scaffold(containerColor = ObsidianBlack) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                item { DashboardTopBar(isEngineRunning = isWakeWordRunning) }

                item {
                    MasterProfileCard(
                        profile = profile,
                        onToggleMasterLock = onToggleMasterLock,
                        onVerifyBiometrics = onVerifyBiometrics
                    )
                }

                item {
                    GpsCopilotCard(
                        isMonitoring = isGpsMonitoring,
                        currentSpeedKmH = currentSpeedKmH,
                        lastAlert = lastAlert,
                        onToggleMonitoring = onToggleGpsMonitoring,
                        onScanRoadAhead = onScanRoadAhead
                    )
                }

                item {
                    Button(
                        onClick = onGrantAllPermissions,
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("grant_all_permissions_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡ CONCEDER TODAS AS PERMISSÕES", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
                        }
                    }
                }

                item {
                    EngineControlCard(
                        isWakeWordRunning = isWakeWordRunning,
                        isOverlayRunning = isOverlayRunning,
                        onToggleWakeWord = onToggleWakeWord,
                        onToggleOverlay = {
                            if (hasOverlayPermission) onToggleOverlay() else showEmbeddedAssistant = !showEmbeddedAssistant
                        }
                    )
                }

                item {
                    PermissionsCard(
                        hasMicPermission = hasMicPermission,
                        hasOverlayPermission = hasOverlayPermission,
                        hasPhonePermission = hasPhonePermission,
                        hasSmsPermission = hasSmsPermission,
                        hasCameraPermission = hasCameraPermission,
                        isBatteryExempt = isBatteryExempt,
                        isAccessibilityEnabled = isAccessibilityEnabled,
                        isNotificationListenerEnabled = isNotificationListenerEnabled,
                        canWriteSettings = canWriteSettings,
                        onGrantMic = onGrantMic,
                        onGrantOverlay = onGrantOverlay,
                        onGrantPhone = onGrantPhone,
                        onGrantSms = onGrantSms,
                        onGrantCamera = onGrantCamera,
                        onGrantBattery = onGrantBattery,
                        onGrantAccessibility = onGrantAccessibility,
                        onGrantNotificationListener = onGrantNotificationListener,
                        onGrantWriteSettings = onGrantWriteSettings
                    )
                }

                item {
                    QuickActionsCard(
                        onActionClick = { cmd ->
                            showEmbeddedAssistant = true
                            assistantViewModel.submitQuery(cmd)
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = showEmbeddedAssistant,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AssistantOverlayContent(
                    viewModel = assistantViewModel,
                    onDismiss = { showEmbeddedAssistant = false }
                )
            }
        }
    }
}
