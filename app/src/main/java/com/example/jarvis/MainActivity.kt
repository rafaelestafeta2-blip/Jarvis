package com.example.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.jarvis.core.background.BatteryOptimizationHelper
import com.example.jarvis.core.background.JarvisAccessibilityService
import com.example.jarvis.core.background.JarvisNotificationListenerService
import com.example.jarvis.core.background.WakeWordForegroundService
import com.example.jarvis.core.memory.JarvisMemoryManager
import com.example.jarvis.core.telemetry.GpsSentinelManager
import com.example.jarvis.core.ui.theme.JarvisTheme
import com.example.jarvis.feature.assistant.ui.AssistantOverlayService
import com.example.jarvis.feature.assistant.viewmodel.AssistantViewModel
import com.example.jarvis.feature.dashboard.ui.DashboardScreen

class MainActivity : ComponentActivity() {

    private val assistantViewModel by viewModels<AssistantViewModel> {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AssistantViewModel(applicationContext) as T
            }
        }
    }

    private val memoryManager by lazy { JarvisMemoryManager.getInstance(this) }
    private val gpsManager by lazy { GpsSentinelManager.getInstance(this) }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results[Manifest.permission.RECORD_AUDIO] == true) {
            Toast.makeText(this, "Permissões concedidas. JARVIS operacional.", Toast.LENGTH_SHORT).show()
            startWakeWordService()
        }
        if (results[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            gpsManager.startSentinelMonitoring()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (hasPermission(Manifest.permission.RECORD_AUDIO)) startWakeWordService()
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) gpsManager.startSentinelMonitoring()

        setContent {
            val profile by memoryManager.profileFlow.collectAsState()
            val isGpsMonitoring by gpsManager.isMonitoring.collectAsState()
            val speed by gpsManager.currentSpeedKmH.collectAsState()
            val lastAlert by gpsManager.lastAlert.collectAsState()

            JarvisTheme {
                DashboardScreen(
                    assistantViewModel = assistantViewModel,
                    profile = profile,
                    isWakeWordRunning = WakeWordForegroundService.isRunning,
                    isOverlayRunning = AssistantOverlayService.isRunning,
                    isGpsMonitoring = isGpsMonitoring,
                    currentSpeedKmH = speed,
                    lastAlert = lastAlert,
                    hasMicPermission = hasPermission(Manifest.permission.RECORD_AUDIO),
                    hasOverlayPermission = hasOverlayPermission(),
                    hasPhonePermission = hasPermission(Manifest.permission.CALL_PHONE),
                    hasSmsPermission = hasPermission(Manifest.permission.SEND_SMS),
                    hasCameraPermission = hasPermission(Manifest.permission.CAMERA),
                    isBatteryExempt = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(this),
                    isAccessibilityEnabled = JarvisAccessibilityService.isRunning,
                    isNotificationListenerEnabled = JarvisNotificationListenerService.isRunning,
                    canWriteSettings = canWriteSettings(),
                    onGrantAllPermissions = { requestAllPermissions() },
                    onToggleWakeWord = { toggleWakeWordService() },
                    onToggleOverlay = { toggleOverlayService() },
                    onToggleGpsMonitoring = { if (it) gpsManager.startSentinelMonitoring() else gpsManager.stopSentinelMonitoring() },
                    onScanRoadAhead = { assistantViewModel.submitQuery("o que tem à minha frente?") },
                    onToggleMasterLock = { memoryManager.toggleMasterLock(it) },
                    onVerifyBiometrics = {
                        Toast.makeText(this, "Identidade biométrica verificada: Bem-vindo, Chefe ${profile.masterName}!", Toast.LENGTH_LONG).show()
                        assistantViewModel.submitQuery("quem sou eu")
                    },
                    onGrantOverlay = { requestOverlayPermission() },
                    onGrantBattery = { BatteryOptimizationHelper.requestIgnoreBatteryOptimizationsIfNeeded(this) },
                    onGrantAccessibility = { openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS) },
                    onGrantNotificationListener = { openSettings(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) },
                    onGrantWriteSettings = { requestWriteSettings() }
                )
            }
        }
    }

    private fun hasPermission(perm: String): Boolean =
        ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED

    private fun hasOverlayPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)

    private fun canWriteSettings(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(this)

    private fun requestAllPermissions() {
        val perms = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(perms.toTypedArray())
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
    }

    private fun requestWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
        }
    }

    private fun openSettings(action: String) {
        try { startActivity(Intent(action)) } catch (_: Exception) {}
    }

    private fun startWakeWordService() {
        val intent = Intent(this, WakeWordForegroundService::class.java).apply { action = WakeWordForegroundService.ACTION_START }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
    }

    private fun toggleWakeWordService() {
        val intent = Intent(this, WakeWordForegroundService::class.java)
        if (WakeWordForegroundService.isRunning) {
            intent.action = WakeWordForegroundService.ACTION_STOP
            stopService(intent)
        } else startWakeWordService()
    }

    private fun toggleOverlayService() {
        val intent = Intent(this, AssistantOverlayService::class.java)
        if (AssistantOverlayService.isRunning) stopService(intent) else {
            intent.action = AssistantOverlayService.ACTION_SHOW_ASSISTANT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
        }
    }
}
