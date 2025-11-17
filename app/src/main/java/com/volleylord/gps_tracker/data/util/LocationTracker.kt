package com.volleylord.gps_tracker.data.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
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

        // Check location permissions before starting
        if (!hasLocationPermission()) {
            close(SecurityException("Location permission required"))
            return@callbackFlow
        }

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

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    Log.w("LocationTracker", "Location services became unavailable")
                }
            }


        }

        // Start location updates
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                context.mainLooper
            )

            Log.d("LocationTracker", "Location updates started successfully")
        } catch (securityException: SecurityException) {
            // Handle permission-related security exception
            Log.e("Location", "SecurityException: Location permission missing", securityException)
        }

        // Start step detection
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        stepSensor?.let {
            val registered = sensorManager.registerListener(stepEventListener, it, SensorManager.SENSOR_DELAY_UI)
            if (registered) {
                Log.d("LocationTracker", "Step detector sensor registered successfully")
            } else {
                Log.w("LocationTracker", "Failed to register step detector sensor")
            }
        } ?: run {
            Log.w("LocationTracker", "Step detector sensor not available on this device")
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
     * Check if location permissions are granted
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * Pauses only step tracking (location continues).
     * Used when session is paused but location tracking should continue.
     */
    fun pauseStepTracking() {
        sensorManager.unregisterListener(stepEventListener)
        // Do NOT reset step count - preserve it for resume
        // Location tracking continues via locationCallback
    }

    /**
     * Resumes step tracking.
     * Location tracking should already be active.
     */
    fun resumeStepTracking() {
        stepSensor?.let {
            val registered = sensorManager.registerListener(stepEventListener, it, SensorManager.SENSOR_DELAY_UI)
            if (registered) {
                Log.d("LocationTracker", "Step detector sensor resumed successfully")
            } else {
                Log.w("LocationTracker", "Failed to resume step detector sensor")
            }
        } ?: run {
            // Re-initialize step sensor if needed
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            stepSensor?.let {
                val registered = sensorManager.registerListener(stepEventListener, it, SensorManager.SENSOR_DELAY_UI)
                if (registered) {
                    Log.d("LocationTracker", "Step detector sensor re-initialized and registered")
                } else {
                    Log.w("LocationTracker", "Failed to register re-initialized step detector sensor")
                }
            } ?: Log.w("LocationTracker", "Step detector sensor not available on this device")
        }
    }

    /**
     * Pauses tracking location and steps without resetting step count.
     * Can be resumed later without losing accumulated data.
     * @deprecated Use pauseStepTracking() for pause functionality
     */
    fun pauseTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        sensorManager.unregisterListener(stepEventListener)
        // Do NOT reset step count - preserve it for resume
        // Do NOT set locationCallback to null - need it for resume
    }

    /**
     * Stops tracking location and steps completely.
     * Resets all state including step count.
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

