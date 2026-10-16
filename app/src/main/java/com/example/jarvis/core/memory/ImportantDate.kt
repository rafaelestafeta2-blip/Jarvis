package com.example.jarvis.core.memory

data class ImportantDate(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val dateString: String,
    val note: String = ""
)
