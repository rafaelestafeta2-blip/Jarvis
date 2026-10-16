package com.example.jarvis.core.speech

interface WakeWordCallback {
    fun onWakeWordDetected(phrase: String, confidence: Float)
    fun onError(errorMessage: String)
}
