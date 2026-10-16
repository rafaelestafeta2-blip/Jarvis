package com.example.jarvis.feature.assistant.engine

import android.util.Log

object TranslationHelper {
    private const val TAG = "TranslationHelper"

    private val ptToEsDictionary = mapOf(
        "bom dia" to "Buenos días",
        "boa tarde" to "Buenas tardes",
        "boa noite" to "Buenas noches",
        "como você está" to "¿Cómo estás?",
        "como vai" to "¿Cómo te va?",
        "obrigado" to "Muchas gracias",
        "muito obrigado" to "Muchas gracias",
        "por favor" to "Por favor",
        "onde fica a farmácia" to "¿Dónde está la farmacia?",
        "onde fica a farmacia" to "¿Dónde está la farmacia?",
        "onde fica o hospital" to "¿Dónde queda el hospital?",
        "quanto custa" to "¿Cuánto cuesta esto?",
        "eu te amo" to "Te quiero mucho",
        "olá" to "Hola, ¿qué tal?",
        "ola" to "Hola, ¿qué tal?",
        "meu nome é" to "Mi nombre es",
        "eu preciso de ajuda" to "Necesito ayuda, por favor"
    )

    private val ptToEnDictionary = mapOf(
        "bom dia" to "Good morning",
        "boa tarde" to "Good afternoon",
        "boa noite" to "Good night",
        "como você está" to "How are you doing?",
        "obrigado" to "Thank you very much",
        "por favor" to "Please",
        "onde fica a farmácia" to "Where is the nearest pharmacy?",
        "onde fica a farmacia" to "Where is the nearest pharmacy?",
        "eu preciso de ajuda" to "I need help, please"
    )

    fun translatePhrase(phrase: String, targetLang: String): String {
        val cleanPhrase = phrase.lowercase().trim()
        val isSpanish = targetLang.contains("espanhol") || targetLang.contains("spanish") || targetLang.contains("es")
        val isEnglish = targetLang.contains("inglês") || targetLang.contains("ingles") || targetLang.contains("en")

        return if (isSpanish) {
            ptToEsDictionary[cleanPhrase] ?: fallbackSpanish(cleanPhrase)
        } else if (isEnglish) {
            ptToEnDictionary[cleanPhrase] ?: fallbackEnglish(cleanPhrase)
        } else {
            "Tradução: $cleanPhrase"
        }
    }

    private fun fallbackSpanish(text: String): String {
        return text
            .replace("bom dia", "buenos días")
            .replace("obrigado", "gracias")
            .replace("olá", "hola")
            .replace("farmácia", "farmacia")
            .replace("onde fica", "dónde está")
            .replace("ajuda", "ayuda")
            .replace("amigo", "amigo")
            .replace("chego em", "llego en")
    }

    private fun fallbackEnglish(text: String): String {
        return text
            .replace("bom dia", "good morning")
            .replace("obrigado", "thank you")
            .replace("olá", "hello")
            .replace("farmácia", "pharmacy")
            .replace("ajuda", "help")
    }
}
