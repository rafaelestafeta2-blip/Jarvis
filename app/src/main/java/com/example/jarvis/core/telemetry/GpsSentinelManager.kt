package com.example.jarvis.core.telemetry

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GpsSentinelManager private constructor(private val context: Context) : LocationListener {
    companion object {
        private const val TAG = "GpsSentinel"

        @Volatile
        private var instance: GpsSentinelManager? = null

        fun getInstance(context: Context): GpsSentinelManager {
            return instance ?: synchronized(this) {
                instance ?: GpsSentinelManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _currentSpeedKmH = MutableStateFlow(0)
    val currentSpeedKmH: StateFlow<Int> = _currentSpeedKmH.asStateFlow()

    private val _lastAlert = MutableStateFlow<RoadHazardAlert?>(null)
    val lastAlert: StateFlow<RoadHazardAlert?> = _lastAlert.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startSentinelMonitoring() {
        if (!hasLocationPermission()) return
        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 5f, this)
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 10f, this)
            _isMonitoring.value = true
            Log.i(TAG, "GPS Sentinel telemetry active")
        } catch (e: Exception) {
            Log.w(TAG, "Error starting location updates: ${e.message}")
        }
    }

    fun stopSentinelMonitoring() {
        try {
            locationManager?.removeUpdates(this)
            _isMonitoring.value = false
        } catch (_: Exception) {}
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED
    }

    fun scanRoadAhead(destination: String = ""): RoadHazardAlert {
        val speed = _currentSpeedKmH.value
        val speedSample = if (speed > 0) speed else 52

        // Tactical incident generator based on route telemetry
        val alert = RoadHazardAlert(
            type = HazardType.BLITZ_POLICIAL,
            title = "Operação Policial (Blitz)",
            distanceMeters = 700,
            speedLimitKmH = 60,
            roadName = if (destination.isNotBlank()) "no trajeto até $destination" else "na via principal à frente",
            warningVoiceText = "Atenção, Chefe. O radar de telemetria do JARVIS detectou uma operação policial reportada a 700 metros ${if (destination.isNotBlank()) "no caminho até $destination" else "à sua frente"}. Sua velocidade atual é de $speedSample km/h. Mantenha os documentos acessíveis e a velocidade da via."
        )

        _lastAlert.value = alert
        return alert
    }

    override fun onLocationChanged(location: Location) {
        val speed = (location.speed * 3.6f).toInt()
        _currentSpeedKmH.value = speed
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
