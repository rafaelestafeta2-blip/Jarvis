package com.example.jarvis.feature.assistant.engine

import android.content.Context
import com.example.jarvis.core.memory.JarvisMemoryManager
import com.example.jarvis.core.memory.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MorningBriefingHelper {

    fun generateBriefingText(profile: UserProfile): String {
        val currentTime = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date())
        val title = profile.masterTitle
        val name = profile.masterName

        val weatherText = "Hoje o céu está limpo, temperatura de 24 graus Celsius com brisa favorável."

        val dates = profile.importantDates
        val dateText = if (dates.isNotEmpty()) {
            "Na sua agenda para hoje: ${dates.first().title}."
        } else {
            "Nenhuma data crítica ou pendência urgente registrada para hoje."
        }

        val messagesText = "O senhor possui 2 novas notificações no WhatsApp aguardando sua resposta."
        val newsText = "Nas manchetes: Grandes avanços no setor de tecnologia e inteligência artificial pelo mundo."
        val finale = "Todos os sistemas do JARVIS OS estão 100% operacionais. É hora de conquistar o mundo, $title. Iniciando sua trilha sonora matinal..."

        return "Bom dia, $title $name. São exatamente $currentTime. $weatherText $dateText $messagesText $newsText $finale"
    }

    fun executeMorningRoutine(
        context: Context,
        onSpeechCallback: (String) -> Unit
    ): String {
        val memoryManager = JarvisMemoryManager.getInstance(context)
        val profile = memoryManager.profileFlow.value
        val speechText = generateBriefingText(profile)

        // 1. Send speech to TTS / UI
        onSpeechCallback(speechText)

        // 2. Play Morning Music track
        MusicPlaybackHelper.playMusic(context, "AC/DC Back in Black")

        return speechText
    }
}
