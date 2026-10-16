package com.example.jarvis.feature.assistant.engine

import android.content.Context
import android.content.Intent
import android.net.Uri

object MusicPlaybackHelper {

    fun playMusic(context: Context, query: String): String {
        val track = if (query.isBlank()) "AC/DC Back in Black" else query

        // Try Spotify first
        try {
            val spotifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:${Uri.encode(track)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (spotifyIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(spotifyIntent)
                return "Iniciando \"$track\" no Spotify agora, senhor. Trilha sonora autorizada."
            }
        } catch (_: Exception) {}

        // Fallback to YouTube Music / Web
        return try {
            val webUri = Uri.parse("https://music.youtube.com/search?q=${Uri.encode(track)}")
            val intent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Iniciando reprodução de \"$track\", senhor. Aproveite a música."
        } catch (e: Exception) {
            "Não foi possível iniciar o tocador de música: ${e.message}"
        }
    }
}
