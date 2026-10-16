package com.example.jarvis

import android.app.Application
import android.util.Log

class JarvisApp : Application() {
    companion object {
        private const val TAG = "JarvisApp"
        lateinit var instance: JarvisApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i(TAG, "JARVIS OS application initialized successfully.")
    }
}
