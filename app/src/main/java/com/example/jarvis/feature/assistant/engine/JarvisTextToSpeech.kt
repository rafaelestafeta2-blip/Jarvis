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
        // Prefer Google TTS engine to avoid Samsung SMT female voice default
        val googleEngine = "com.google.android.tts"
        tts = try {
            TextToSpeech(context.applicationContext, this, googleEngine)
        } catch (_: Exception) {
            TextToSpeech(context.applicationContext, this)
        }
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
            }

            // Select Male Voice in Portuguese
            configureMaleVoice()

            // Jarvis iconic deep, baritone, calm resonance
            tts?.setPitch(0.70f)
            tts?.setSpeechRate(0.96f)
            setupListener()
            isInitialized = true
            Log.i(tag, "JarvisTextToSpeech initialized with deep baritone tone")
        } else {
            // Fallback to default engine if Google TTS failed to init
            try {
                tts = TextToSpeech(context.applicationContext) { fallbackStatus ->
                    if (fallbackStatus == TextToSpeech.SUCCESS) {
                        tts?.setLanguage(Locale("pt", "BR"))
                        configureMaleVoice()
                        tts?.setPitch(0.70f)
                        tts?.setSpeechRate(0.96f)
                        setupListener()
                        isInitialized = true
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun configureMaleVoice() {
        try {
            val voices = tts?.voices
            if (!voices.isNullOrEmpty()) {
                val maleVoice = voices.firstOrNull { v ->
                    v.locale.language == "pt" && (
                        v.name.contains("male", ignoreCase = true) ||
                        v.name.contains("homem", ignoreCase = true) ||
                        v.name.contains("ptd", ignoreCase = true) ||
                        v.name.contains("yfs", ignoreCase = true) ||
                        v.name.contains("#male", ignoreCase = true)
                    )
                } ?: voices.firstOrNull { v ->
                    v.locale.language == "pt" &&
                    !v.name.contains("female", ignoreCase = true) &&
                    !v.name.contains("mulher", ignoreCase = true)
                }

                if (maleVoice != null) {
                    tts?.voice = maleVoice
                    Log.i(tag, "Selected Jarvis male voice: ${maleVoice.name}")
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Error selecting male voice: ${e.message}")
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
        } catch (_: Exception) {}
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
