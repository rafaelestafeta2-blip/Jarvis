package com.example.jarvis.core.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.jarvis.core.speech.AudioRecordWakeWordDetector
import com.example.jarvis.core.speech.GlobalAssistantStateMachine
import com.example.jarvis.core.speech.WakeWordCallback
import com.example.jarvis.feature.assistant.ui.AssistantOverlayService

class WakeWordForegroundService : Service(), WakeWordCallback {
    companion object {
        private const val TAG = "WakeWordService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "jarvis_wake_word_channel"

        const val ACTION_START = "com.example.jarvis.action.START_SERVICE"
        const val ACTION_STOP = "com.example.jarvis.action.STOP_SERVICE"

        @Volatile
        var isRunning = false
            private set
    }

    private var detector: AudioRecordWakeWordDetector? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        if (action == ACTION_STOP) {
            stopServiceInternal()
            return START_NOT_STICKY
        }

        startForegroundServiceInternal()
        return START_STICKY
    }

    private fun startForegroundServiceInternal() {
        val notification = createNotification()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        } else 0

        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
        isRunning = true

        startDetector()
    }

    private fun startDetector() {
        try {
            detector?.stopListening()
            detector?.release()
        } catch (_: Exception) {}

        detector = AudioRecordWakeWordDetector().apply {
            if (initialize(this@WakeWordForegroundService)) {
                startListening(this@WakeWordForegroundService)
                Log.i(TAG, "AudioRecordWakeWordDetector started")
            }
        }
    }

    override fun onWakeWordDetected(phrase: String, confidence: Float) {
        Log.i(TAG, "Wake word detected: $phrase (confidence: $confidence)")
        vibrateFeedback()

        detector?.pauseListening()
        GlobalAssistantStateMachine.transitionToSync(GlobalAssistantStateMachine.AssistantState.WAKEDETECTED)

        // Launch the Assistant Overlay
        val overlayIntent = Intent(this, AssistantOverlayService::class.java).apply {
            action = AssistantOverlayService.ACTION_SHOW_ASSISTANT
            putExtra("wake_phrase", phrase)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(overlayIntent)
        } else {
            startService(overlayIntent)
        }
    }

    override fun onError(errorMessage: String) {
        Log.w(TAG, "Wake word error: $errorMessage")
    }

    private fun vibrateFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibrate feedback failed", e)
        }
    }

    private fun stopServiceInternal() {
        isRunning = false
        try {
            detector?.stopListening()
            detector?.release()
            detector = null
        } catch (_: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopServiceInternal()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JARVIS Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background listening engine for JARVIS voice commands"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JARVIS OS")
            .setContentText("Listening for 'Jarvis' wake-word...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
