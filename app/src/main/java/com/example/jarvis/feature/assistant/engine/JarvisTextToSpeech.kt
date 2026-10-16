package com.example.jarvis.feature.assistant.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class JarvisTextToSpeech(
    private val context: Context,
    private val onSpeakingStateChanged: (Boolean) -> Unit = {}
) : TextToSpeech.OnInitListener {

    private val tag = "JarvisTTS"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ptLocale = Locale("pt", "BR")
            val supported = tts?.isLanguageAvailable(ptLocale)

            if (supported != TextToSpeech.LANG_NOT_SUPPORTED && supported != TextToSpeech.LANG_MISSING_DATA) {
                tts?.setLanguage(ptLocale)
                Log.i(tag, "TTS configured for Portuguese (Brazil)")
            } else {
                val defaultLocale = Locale.getDefault()
                tts?.setLanguage(defaultLocale)
                Log.i(tag, "TTS fallback to ${defaultLocale.displayName}")
            }

            // Jarvis iconic voice cadence: slightly deeper pitch and calm cadence
            tts?.setPitch(0.92f)
            tts?.setSpeechRate(1.02f)
            setupListener()
            isInitialized = true
        } else {
            Log.e(tag, "Failed to initialize TextToSpeech: status $status")
        }
    }

    private fun setupListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onSpeakingStateChanged(true)
            }

            override fun onDone(utteranceId: String?) {
                onSpeakingStateChanged(false)
            }

            override fun onError(utteranceId: String?) {
                onSpeakingStateChanged(false)
            }
        })
    }

    fun speak(text: String) {
        if (!isInitialized || text.isBlank()) return
        val utteranceId = "tts_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
            onSpeakingStateChanged(false)
        } catch (e: Exception) {
            Log.w(tag, "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
