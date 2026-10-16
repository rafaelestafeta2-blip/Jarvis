package com.example.jarvis.core.speech

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale
import kotlin.concurrent.thread

class AudioRecordWakeWordDetector : WakeWordDetector {
    companion object {
        private const val TAG = "JarvisWakeDetector"
        private const val SAMPLE_RATE = 16000
        private const val VOICE_ENERGY_THRESHOLD = 14000
        private const val RESTART_COOLDOWN_MS = 4000L
    }

    private var appContext: Context? = null
    private var wakeWordCallback: WakeWordCallback? = null
    private val audioManager by lazy { appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    @Volatile private var isInitialized = false
    @Volatile private var isActive = false
    @Volatile private var isPaused = false
    @Volatile private var isRecognizing = false
    private var audioRecord: AudioRecord? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun initialize(context: Context): Boolean {
        if (isInitialized) return true
        appContext = context.applicationContext
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return false
        isInitialized = true
        return true
    }

    override fun startListening(callback: WakeWordCallback) {
        if (!isInitialized) return
        wakeWordCallback = callback
        isActive = true
        isPaused = false
        startSilentAudioMonitor()
    }

    override fun isListening(): Boolean = isActive && !isPaused
    override fun getCurrentConfidence(): Float = 0.95f

    fun pauseListening() {
        isPaused = true
        stopAudioRecord()
        mainHandler.post {
            unmuteSystemSounds()
            try { speechRecognizer?.cancel() } catch (_: Exception) {}
            isRecognizing = false
        }
    }

    fun resumeListening() {
        if (!isInitialized || !isActive) return
        isPaused = false
        startSilentAudioMonitor()
    }

    override fun stopListening() {
        isActive = false
        isPaused = false
        stopAudioRecord()
        mainHandler.post {
            unmuteSystemSounds()
            try { speechRecognizer?.destroy() } catch (_: Exception) {}
            speechRecognizer = null
            isRecognizing = false
        }
    }

    override fun release() { stopListening(); isInitialized = false }

    @SuppressLint("MissingPermission")
    private fun startSilentAudioMonitor() {
        if (!isActive || isPaused || isRecognizing) return
        stopAudioRecord()
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (bufferSize <= 0) return

        try {
            audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
            audioRecord?.startRecording()
            thread(name = "JarvisVADThread") {
                val buffer = ShortArray(bufferSize)
                var speechFramesCount = 0
                while (isActive && !isPaused && !isRecognizing) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        var maxAmp = 0
                        for (i in 0 until read) {
                            val abs = kotlin.math.abs(buffer[i].toInt())
                            if (abs > maxAmp) maxAmp = abs
                        }
                        if (maxAmp > VOICE_ENERGY_THRESHOLD) {
                            speechFramesCount++
                            if (speechFramesCount >= 2) {
                                stopAudioRecord()
                                mainHandler.post { launchSpeechRecognition() }
                                break
                            }
                        } else speechFramesCount = 0
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun stopAudioRecord() {
        try { audioRecord?.stop(); audioRecord?.release() } catch (_: Exception) {}
        audioRecord = null
    }

    private fun muteSystemSounds() {
        try {
            audioManager?.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            audioManager?.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
        } catch (_: Exception) {}
    }

    private fun unmuteSystemSounds() {
        try {
            audioManager?.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
            audioManager?.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
        } catch (_: Exception) {}
    }

    private fun launchSpeechRecognition() {
        if (!isActive || isPaused || isRecognizing) return
        isRecognizing = true
        muteSystemSounds()
        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply { setRecognitionListener(createListener()) }
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext?.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            isRecognizing = false
            unmuteSystemSounds()
            startSilentAudioMonitor()
        }
    }

    private fun createListener(): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { unmuteSystemSounds() }
        override fun onError(error: Int) {
            unmuteSystemSounds()
            isRecognizing = false
            mainHandler.postDelayed({ startSilentAudioMonitor() }, RESTART_COOLDOWN_MS)
        }
        override fun onResults(results: Bundle?) {
            unmuteSystemSounds()
            isRecognizing = false
            processMatches(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
            mainHandler.postDelayed({ startSilentAudioMonitor() }, RESTART_COOLDOWN_MS)
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
            if (text.contains("jarvis") || text.contains("javis") || text.contains("jarves") || text.contains("jarbas")) {
                pauseListening()
                wakeWordCallback?.onWakeWordDetected(text, 0.95f)
                break
            }
        }
    }
}
