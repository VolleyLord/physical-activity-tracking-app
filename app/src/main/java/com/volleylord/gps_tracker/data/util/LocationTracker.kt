package com.volleylord.gps_tracker.data.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks GPS location and step count.
 * Combines FusedLocationProvider for GPS and SensorManager for step detection.
 */
@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val sensorManager: SensorManager
) {
    private val _stepCount = MutableStateFlow(0L)
    val stepCount: StateFlow<Long> = _stepCount.asStateFlow()

    private var stepSensor: Sensor? = null
    private var stepListener: SensorEventListener? = null
    private var locationCallback: LocationCallback? = null

    private val stepEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
                _stepCount.update { it + 1 }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // No-op
        }
    }

    /**
     * Starts tracking location and steps.
     * Returns a Flow of TrackingPoints.
     */

    fun startTracking(sessionStartTime: Long): Flow<TrackingPoint> = callbackFlow {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(10000)
            .build()

        var previousLocation: Location? = null
        var previousTime = sessionStartTime

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val currentTime = System.currentTimeMillis()
                
                val elapsed = (currentTime - sessionStartTime).milliseconds
                
                val point = TrackingPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitudeMeters = if (location.hasAltitude()) location.altitude else null,
                    timestampEpochMillis = currentTime,
                    elapsedTime = elapsed
                )

                previousLocation = location
                previousTime = currentTime
                trySend(point)
            }
        }

        // Start location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            context.mainLooper
        )

        // Start step detection
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        stepSensor?.let {
            sensorManager.registerListener(stepEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose {
            locationCallback?.let {
                fusedLocationClient.removeLocationUpdates(it)
            }
            sensorManager.unregisterListener(stepEventListener)
            locationCallback = null
        }
    }

    /**
     * Stops tracking location and steps.
     */
    fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        sensorManager.unregisterListener(stepEventListener)
        locationCallback = null
        _stepCount.value = 0L
    }

    /**
     * Resets step count to zero.
     */
    fun resetStepCount() {
        _stepCount.value = 0L
    }
}

