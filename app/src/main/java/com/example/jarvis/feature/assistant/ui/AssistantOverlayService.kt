package com.example.jarvis.feature.assistant.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.jarvis.core.ui.theme.JarvisTheme
import com.example.jarvis.feature.assistant.viewmodel.AssistantViewModel

class AssistantOverlayService : Service() {
    companion object {
        private const val TAG = "JarvisOverlay"
        const val ACTION_SHOW_ASSISTANT = "com.example.jarvis.action.SHOW_ASSISTANT"
        const val ACTION_HIDE_ASSISTANT = "com.example.jarvis.action.HIDE_ASSISTANT"
        const val ACTION_TOGGLE = "com.example.jarvis.action.TOGGLE_OVERLAY"

        private const val CHANNEL_ID = "jarvis_overlay_channel"
        private const val NOTIFICATION_ID = 1002

        @Volatile
        var isRunning = false
            private set
    }

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var viewModel: AssistantViewModel? = null
    private val lifecycleOwner = OverlayLifecycleOwner()
    private var isOverlayShowing = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startForegroundWithNotification()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        viewModel = AssistantViewModel(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_ASSISTANT -> showOverlay()
            ACTION_HIDE_ASSISTANT -> hideOverlay()
            ACTION_TOGGLE -> if (isOverlayShowing) hideOverlay() else showOverlay()
            else -> showOverlay()
        }
        return START_STICKY
    }

    fun showOverlay() {
        if (isOverlayShowing) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Cannot draw overlay - SYSTEM_ALERT_WINDOW permission missing")
            return
        }

        try {
            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.BOTTOM
            }

            overlayView = ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeViewModelStoreOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                setContent {
                    JarvisTheme {
                        viewModel?.let { vm ->
                            AssistantOverlayContent(
                                viewModel = vm,
                                onDismiss = { hideOverlay() }
                            )
                        }
                    }
                }
            }

            windowManager?.addView(overlayView, params)
            isOverlayShowing = true
            Log.i(TAG, "Overlay window displayed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display overlay view", e)
        }
    }

    fun hideOverlay() {
        if (!isOverlayShowing || overlayView == null) return
        try {
            windowManager?.removeView(overlayView)
            overlayView = null
            isOverlayShowing = false
            Log.i(TAG, "Overlay window removed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove overlay view", e)
        }
    }

    private fun startForegroundWithNotification() {
        createNotificationChannel()
        val notification = createNotification()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JARVIS Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service maintaining floating assistant overlay"
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
            .setContentTitle("JARVIS Assistant")
            .setContentText("Overlay window active")
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        hideOverlay()
        lifecycleOwner.destroy()
        isRunning = false
        super.onDestroy()
    }
}
