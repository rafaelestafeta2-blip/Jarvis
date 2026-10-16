package com.example.jarvis.core.background

import android.content.Context
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.jarvis.feature.assistant.engine.JarvisTextToSpeech

object NotificationAnnouncer {
    private const val TAG = "NotificationAnnouncer"
    private var textToSpeech: JarvisTextToSpeech? = null
    var isVoiceAnnouncementEnabled: Boolean = true

    fun initialize(context: Context) {
        if (textToSpeech == null) {
            textToSpeech = JarvisTextToSpeech(context.applicationContext)
        }
    }

    fun handleNotification(sbn: StatusBarNotification) {
        if (!isVoiceAnnouncementEnabled) return

        val pkg = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        when {
            // WhatsApp Message Detection
            pkg == "com.whatsapp" -> {
                // Avoid group count summary notifications
                if (text.isNotBlank() && !text.contains("novas mensagens") && !text.contains("new messages")) {
                    val announcement = "Senhor, você recebeu uma mensagem de $title no WhatsApp: \"$text\". Deseja que eu responda?"
                    Log.i(TAG, "Announcing WhatsApp message: $announcement")
                    textToSpeech?.speak(announcement)
                }
            }

            // Incoming Call Detection
            pkg.contains("dialer") || pkg.contains("incallui") || pkg.contains("telecom") || sbn.notification.category == "call" -> {
                val caller = if (title.isNotBlank()) title else "número desconhecido"
                val announcement = "Senhor, você está recebendo uma chamada de $caller. Deseja atender?"
                Log.i(TAG, "Announcing incoming call from: $caller")
                textToSpeech?.speak(announcement)
            }

            // SMS Message Detection
            pkg.contains("messaging") || pkg.contains("mms") || sbn.notification.category == "msg" -> {
                val announcement = "Senhor, novo SMS recebido de $title: \"$text\"."
                Log.i(TAG, "Announcing SMS: $announcement")
                textToSpeech?.speak(announcement)
            }
        }
    }

    fun shutdown() {
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
