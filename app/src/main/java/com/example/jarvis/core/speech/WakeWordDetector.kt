package com.example.jarvis.core.speech

import android.content.Context

interface WakeWordDetector {
    fun initialize(context: Context): Boolean
    fun startListening(callback: WakeWordCallback)
    fun stopListening()
    fun release()
    fun isListening(): Boolean
    fun getCurrentConfidence(): Float
}
