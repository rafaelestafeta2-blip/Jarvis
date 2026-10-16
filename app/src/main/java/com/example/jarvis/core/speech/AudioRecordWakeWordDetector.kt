package com.example.jarvis.core.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class AudioRecordWakeWordDetector : WakeWordDetector {
    companion object {
        private const val TAG = "JarvisWakeDetector"
        private const val RESTART_DELAY_MS = 250L
    }

    private var appContext: Context? = null
    private var wakeWordCallback: WakeWordCallback? = null
    @Volatile private var isInitialized = false
    @Volatile private var isCurrentlyListening = false
    @Volatile private var isPaused = false
    @Volatile private var isActive = false
    @Volatile private var currentConfidence = 0.95f
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    // Portuguese and English wake word variants
    private val wakeWords = listOf(
        "jarvis", "ei jarvis", "olá jarvis", "ola jarvis", "fala jarvis", "ô jarvis", "o jarvis",
        "hey jarvis", "ok jarvis", "okay jarvis", "javis", "hey javis", "jarves"
    )

    override fun initialize(context: Context): Boolean {
        if (isInitialized) return true
        appContext = context.applicationContext
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "RECORD_AUDIO permission missing")
            return false
        }
        if (!SpeechRecognizer.isRecognitionAvailable(appContext!!)) {
            Log.e(TAG, "SpeechRecognizer not available on device")
            return false
        }
        isInitialized = true
        return true
    }

    override fun startListening(callback: WakeWordCallback) {
        if (!isInitialized) return
        if (isCurrentlyListening && !isPaused) return
        wakeWordCallback = callback
        isCurrentlyListening = true
        isPaused = false
        isActive = true
        mainHandler.post { initAndStartRecognizer() }
    }

    override fun isListening(): Boolean = isCurrentlyListening && !isPaused
    override fun getCurrentConfidence(): Float = currentConfidence

    fun pauseListening() {
        isPaused = true
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w(TAG, "Stop error on pause: ${e.message}")
            }
        }
    }

    fun resumeListening() {
        if (!isInitialized || !isActive) return
        isPaused = false
        isCurrentlyListening = true
        mainHandler.post { initAndStartRecognizer() }
    }

    override fun stopListening() {
        isActive = false
        isCurrentlyListening = false
        isPaused = false
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.post { destroyRecognizer() }
    }

    override fun release() {
        isActive = false
        isCurrentlyListening = false
        isPaused = false
        isInitialized = false
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.post { destroyRecognizer() }
    }

    private fun initAndStartRecognizer() {
        if (!isActive || isPaused) return
        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
                    setRecognitionListener(createListener())
                }
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext?.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recognizer: ${e.message}")
            scheduleRestart(1000L)
        }
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error destroying speech recognizer: ${e.message}")
        }
        speechRecognizer = null
    }

    private fun scheduleRestart(delayMs: Long = RESTART_DELAY_MS) {
        if (!isActive || isPaused) return
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            if (isActive && !isPaused) {
                initAndStartRecognizer()
            }
        }, delayMs)
    }

    private fun createListener(): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            if (!isActive || isPaused) return
            if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
                destroyRecognizer()
            }
            scheduleRestart(RESTART_DELAY_MS)
        }
        override fun onResults(results: Bundle?) {
            processMatches(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
            scheduleRestart(RESTART_DELAY_MS)
        }
        override fun onPartialResults(partialResults: Bundle?) {
            processMatches(partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
        }
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun processMatches(matches: List<String>?) {
        if (matches.isNullOrEmpty() || !isActive || isPaused) return
        for (hypothesis in matches) {
            val text = hypothesis.lowercase().trim()
            val detected = text.contains("jarvis") || text.contains("javis") ||
                           text.contains("jarves") || text.contains("jarbas") ||
                           text.contains("jarvi") || text.contains("iarvis") ||
                           wakeWords.any { text.contains(it) }
            if (detected) {
                Log.i(TAG, "🔥 WAKE WORD DETECTED: '$text'")
                currentConfidence = 0.95f
                wakeWordCallback?.onWakeWordDetected(text, currentConfidence)
                break
            }
        }
    }
}
