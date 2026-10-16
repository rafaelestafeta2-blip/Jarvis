package com.example.jarvis.feature.assistant.engine

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock

object AlarmHelper {

    fun setupAlarm(context: Context, text: String, masterTitle: String): String {
        val (hour, minute) = parseHourAndMinute(text)

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, "Despertador JARVIS")
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            val minStr = if (minute == 0) "00" else minute.toString()
            "Alarme programado com sucesso para as ${hour}h${minStr}, $masterTitle. Deixe o monitoramento noturno comigo. Amanhã o acordarei com o relatório do sistema e uma mensagem inspiradora à altura do Tony Stark."
        } catch (_: Exception) {
            "Alarme pré-configurado para as ${hour}h, $masterTitle. Verifique as permissões de relógio."
        }
    }

    private fun parseHourAndMinute(text: String): Pair<Int, Int> {
        val lower = text.lowercase()
        val regex = Regex("(\\d{1,2})(:|h)?(\\d{2})?")
        val match = regex.find(lower)

        var hour = 7
        var min = 0

        if (match != null) {
            hour = match.groupValues[1].toIntOrNull() ?: 7
            min = match.groupValues[3].toIntOrNull() ?: 0
        } else {
            if (lower.contains("seis")) hour = 6
            if (lower.contains("sete")) hour = 7
            if (lower.contains("oito")) hour = 8
            if (lower.contains("nove")) hour = 9
            if (lower.contains("meia")) min = 30
        }

        if (hour > 23) hour = 7
        return Pair(hour, min)
    }
}
