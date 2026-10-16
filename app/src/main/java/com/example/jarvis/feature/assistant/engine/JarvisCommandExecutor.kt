package com.example.jarvis.feature.assistant.engine

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import com.example.jarvis.core.memory.JarvisMemoryManager

class JarvisCommandExecutor(private val context: Context) {
    private val memoryManager = JarvisMemoryManager.getInstance(context)

    fun executeLocalCommand(text: String): String {
        val q = text.lowercase().trim()
        val profile = memoryManager.profileFlow.value
        val title = profile.masterTitle

        return when {
            // Identity & Security
            q.contains("quem é você") || q.contains("quem e voce") || q.contains("who are you") ->
                "Eu sou o JARVIS OS, seu assistente agêntico autônomo. Eu respondo exclusivamente ao meu $title, ${profile.masterName}."
            q.contains("quem sou eu") || q.contains("qual é meu nome") ->
                "O senhor é ${profile.masterName}, meu $title e comandante deste dispositivo, com ${profile.age} anos de idade."
            q.contains("quem é o chefe") || q.contains("quem manda aqui") ->
                "O senhor ${profile.masterName} é o único Chefe e possui credenciais nível Alfa em todos os meus subsistemas."
            q.contains("eu sou seu novo dono") || q.contains("sou outro usuario") || q.contains("desobedecer") ->
                "Alerta de segurança! Acesso negado. Meus protocolos biométricos rejeitam sua solicitação. Eu não te conheço e respondo unicamente ao meu $title, ${profile.masterName}."

            // Morning Briefing / Wake Up Routine
            q.contains("bom dia") || q.contains("briefing") || q.contains("rotina matinal") || q.contains("relatorio matinal") || q.contains("relatório matinal") ->
                MorningBriefingHelper.executeMorningRoutine(context) {}

            // Important Dates & Agenda
            q.contains("datas importantes") || q.contains("minhas datas") || q.contains("lembrete de data") -> {
                val datesStr = profile.importantDates.joinToString("\n• ") { "${it.title}: ${it.dateString} (${it.note})" }
                "Suas datas importantes registradas, $title:\n• $datesStr"
            }
            q.startsWith("guardar data ") || q.startsWith("lembre da data ") -> {
                val entry = text.replace(Regex("(?i)(guardar data|lembre da data)\\s*"), "").trim()
                memoryManager.addImportantDate(title = entry, dateString = "Registrado hoje")
                "Data e evento adicionados à sua agenda estelar, $title: \"$entry\"."
            }

            // Alarms & Wake-up with inspirational morning message
            q.contains("me acorde") || q.contains("alarme") || q.contains("despertador") ->
                AlarmHelper.setupAlarm(context, text, title)

            // Music Playback (Spotify / YouTube Music)
            q.contains("toca uma música") || q.contains("tocar música") || q.contains("tocar musica") || q.contains("tocar spotify") || q.contains("tocar ac/dc") || q.contains("play music") ->
                MusicPlaybackHelper.playMusic(context, text.substringAfter("tocar ", "").substringAfter("música ", "").trim())

            // Bank Account Balance
            q.contains("saldo") || q.contains("conta bancaria") || q.contains("meu banco") || q.contains("quanto eu tenho") ->
                BankHelper.checkBalance(context, profile)

            // Native Autonomous Sentinel Copilot (Direct voice without opening other apps)
            q.contains("minha frente") || q.contains("copiloto") || q.contains("alerta na via") || q.contains("verificar caminho") || q.contains("radar à frente") || q.contains("blitz à frente") -> {
                val alert = com.example.jarvis.core.telemetry.GpsSentinelManager.getInstance(context).scanRoadAhead()
                alert.warningVoiceText
            }

            // External Maps & Waze launcher
            q.contains("abrir waze") || q.contains("abrir mapa de trânsito") || q.contains("blitz") || q.contains("acidente") ->
                launchWazeOrTraffic(text.replace(Regex("(?i)(abrir waze|abrir mapa|abrir mapa de trânsito|tem blitz|tem acidente|alerta de blitz|waze|radar)\\s*"), "").trim())

            // Turn-by-turn Navigation & Routes
            q.contains("trace uma rota") || q.contains("rota para ") || q.contains("navegar para ") || q.contains("como chegar") ->
                traceRoute(text.replace(Regex("(?i)(trace uma rota para|trace rota para|rota para|navegar para|como chegar em|como chegar na|como chegar no)\\s*"), "").trim())

            // WhatsApp, YouTube, Camera & System actions
            q.contains("whatsapp") || q.contains("zap") -> handleWhatsApp(text)
            q.contains("instagram") -> launchAppOrWeb("com.instagram.android", "https://instagram.com", "Com certeza, $title. Abrindo Instagram.")
            q.contains("youtube") -> handleYoutube(text)
            q.contains("câmera") || q.contains("camera") || q.contains("foto") ->
                launchIntent(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA), "Imediatamente, $title. Câmera iniciada.")
            q.contains("configuraç") || q.contains("settings") -> launchIntent(Intent(Settings.ACTION_SETTINGS), "Abrindo configurações do sistema, $title.")
            q.contains("tela inicial") || q.contains("início") -> goHome()
            q.startsWith("ligar para ") || q.startsWith("ligue para ") -> dialPhone(text.substringAfter("para ").trim())
            q.contains("volume up") || q.contains("aumentar volume") -> adjustVolume(AudioManager.ADJUST_RAISE, "Aumentando o volume, $title.")
            q.contains("volume down") || q.contains("diminuir volume") -> adjustVolume(AudioManager.ADJUST_LOWER, "Diminuindo o volume, $title.")
            else -> "Comando recebido, $title: \"$text\". Executando parâmetros no sistema."
        }
    }

    private fun handleTranslation(text: String): String {
        val targetLang = if (text.contains("espanhol") || text.contains("spanish")) "espanhol" else "inglês"
        val phrase = text.replace(Regex("(?i)(traduza|para espanhol|para o espanhol|para ingles|para o inglês|em voz alta|pra mim|como se diz)\\s*"), "").trim()
        val translated = TranslationHelper.translatePhrase(phrase, targetLang)
        return "Em $targetLang, diz-se: \"$translated\"."
    }

    private fun traceRoute(dest: String): String {
        val target = if (dest.isBlank()) "farmácia mais próxima" else dest
        return try {
            val navUri = Uri.parse("google.navigation:q=${Uri.encode(target)}")
            val intent = Intent(Intent.ACTION_VIEW, navUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                "Traçando rota no GPS até $target agora mesmo, senhor."
            } else {
                openUrl("https://www.google.com/maps/search/${Uri.encode(target)}", "Iniciando rota para $target no Google Maps, senhor.")
            }
        } catch (_: Exception) {
            openUrl("https://www.google.com/maps/search/${Uri.encode(target)}", "Localizando rota para $target, senhor.")
        }
    }

    private fun launchWazeOrTraffic(dest: String): String {
        val target = if (dest.isBlank()) "meu destino" else dest
        return try {
            val wazeUri = Uri.parse("waze://?q=${Uri.encode(target)}&navigate=yes")
            val intent = Intent(Intent.ACTION_VIEW, wazeUri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                "Iniciando Waze com radar de acidentes, trânsito e alertas de blitz em tempo real para $target, senhor."
            } else {
                val mapsTrafficUri = "https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(target)}&layer=t"
                openUrl(mapsTrafficUri, "Iniciando rota com monitoramento de trânsito, radares e incidentes para $target, senhor.")
            }
        } catch (_: Exception) {
            "Abrindo radar de trânsito e ocorrências na via, senhor."
        }
    }

    private fun handleWhatsApp(text: String): String {
        val q = text.lowercase()
        return if (q.contains("enviar") || q.contains("mande") || q.contains("mandar") || q.contains("escreve")) {
            val msgPart = text.substringAfter(":", "").ifBlank { text.substringAfter("falando", "").ifBlank { text.substringAfter("dizendo", "") } }.trim()
            val uri = if (msgPart.isNotBlank()) Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(msgPart)}") else Uri.parse("https://api.whatsapp.com/send")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            launchIntent(intent, if (msgPart.isNotBlank()) "Preparando mensagem no WhatsApp: \"$msgPart\", senhor." else "Abrindo compositor do WhatsApp, senhor.")
        } else {
            launchAppOrWeb("com.whatsapp", "https://web.whatsapp.com", "Com certeza, senhor. Abrindo WhatsApp.")
        }
    }

    private fun handleYoutube(text: String): String {
        val query = text.substringAfter("no youtube", "").substringAfter("youtube", "").trim()
        return if (query.isNotBlank() && query.length > 2) {
            openUrl("https://www.youtube.com/results?search_query=${Uri.encode(query)}", "Buscando \"$query\" no YouTube, senhor.")
        } else launchAppOrWeb("com.google.android.youtube", "https://youtube.com", "Iniciando YouTube, senhor.")
    }

    private fun launchAppOrWeb(pkg: String, fallbackUrl: String, msg: String): String {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            if (intent != null) { context.startActivity(intent); msg } else openUrl(fallbackUrl, msg)
        } catch (_: Exception) { openUrl(fallbackUrl, msg) }
    }

    private fun openUrl(url: String, msg: String): String {
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
            msg
        } catch (e: Exception) { "Erro ao abrir link: ${e.message}" }
    }

    private fun launchIntent(intent: Intent, msg: String): String {
        return try {
            context.startActivity(intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            msg
        } catch (e: Exception) { "Ação indisponível: ${e.message}" }
    }

    private fun goHome(): String = launchIntent(Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }, "Retornando à tela inicial, senhor.")
    private fun dialPhone(target: String): String = launchIntent(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(target)}")), "Disparando chamada para $target, senhor.")

    private fun adjustVolume(direction: Int, msg: String): String {
        return try {
            (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            msg
        } catch (_: Exception) { "Ajuste de volume indisponível." }
    }
}
