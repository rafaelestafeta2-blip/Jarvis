package com.example.jarvis.core.telemetry

enum class HazardType {
    BLITZ_POLICIAL,
    ACIDENTE,
    RADAR_FIXO,
    OBRAS_PISTA,
    VIA_LIVRE
}

data class RoadHazardAlert(
    val type: HazardType,
    val title: String,
    val distanceMeters: Int,
    val speedLimitKmH: Int,
    val roadName: String,
    val warningVoiceText: String
)
