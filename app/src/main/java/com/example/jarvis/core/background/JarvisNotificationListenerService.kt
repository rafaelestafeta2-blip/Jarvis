package com.example.jarvis.core.background

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class JarvisNotificationListenerService : NotificationListenerService() {
    companion object {
        private const val TAG = "JarvisNotification"
        @Volatile
        var isRunning: Boolean = false
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isRunning = true
        NotificationAnnouncer.initialize(this)
        Log.i(TAG, "JarvisNotificationListenerService connected and initialized.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            val pkg = it.packageName
            val extras = it.notification.extras
            val title = extras.getString("android.title") ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            Log.d(TAG, "Notification received from [$pkg]: $title - $text")
            NotificationAnnouncer.handleNotification(it)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isRunning = false
        Log.i(TAG, "JarvisNotificationListenerService disconnected.")
    }

    override fun onDestroy() {
        NotificationAnnouncer.shutdown()
        super.onDestroy()
    }
}
