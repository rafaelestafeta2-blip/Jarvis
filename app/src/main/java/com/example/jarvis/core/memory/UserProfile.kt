package com.example.jarvis.core.memory

data class UserProfile(
    val masterName: String = "Rafael",
    val masterTitle: String = "Chefe",
    val age: Int = 30,
    val isMasterLockEnabled: Boolean = false,
    val isBiometricAuthenticated: Boolean = true,
    val accountBalance: Double = 12500.0,
    val memories: Map<String, String> = mapOf(
        "cargo" to "Chefe Supremo e Criador",
        "preferencia_cafe" to "Forte e sem açúcar",
        "seguranca" to "Apenas o Mestre tem autorização nível Alfa"
    ),
    val importantDates: List<ImportantDate> = listOf(
        ImportantDate(title = "Aniversário do Chefe", dateString = "15 de Novembro", note = "Festa principal"),
        ImportantDate(title = "Reunião de Diretoria Stark", dateString = "Segunda-feira 09:00", note = "Apresentação executiva"),
        ImportantDate(title = "Pagamento de Proventos", dateString = "Dia 05 de cada mês", note = "Verificação de investimentos")
    )
)
