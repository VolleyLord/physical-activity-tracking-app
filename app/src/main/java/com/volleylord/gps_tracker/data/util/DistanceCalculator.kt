package com.volleylord.gps_tracker.data.util

import android.location.Location
import android.util.Log
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import kotlin.math.abs

/**
 * Utility for calculating distance using GPS data with step-based fallback.
 * Simple and reliable approach for walking tracking.
 */
object DistanceCalculator {

    private const val MAX_TIME_DIFF_MS = 30000L
    private const val MIN_WALKING_SPEED = 0.5
    private const val MAX_WALKING_SPEED = 4.0
    private const val AVERAGE_STRIDE_LENGTH = 0.75

    // Track the current calculation mode for the route
    private var currentMode: CalculationMode = CalculationMode.GPS
    private var modeSwitchPoint: Int = 0
    private var stepCountAtSwitch: Long = 0L

    private enum class CalculationMode {
        GPS, STEP_BASED
    }

    fun calculateDistance(point1: TrackingPoint, point2: TrackingPoint): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            point1.latitude, point1.longitude,
            point2.latitude, point2.longitude,
            results
        )
        return results[0].toDouble()
    }

    /**
     * Calculates total distance with smart mode switching.
     */
    fun calculateTotalDistance(route: List<TrackingPoint>, stepCount: Long = 0L): Double {
        if (route.size < 2) return 0.0

        // Reset mode if starting new route
        if (route.size <= 2) {
            currentMode = CalculationMode.GPS
            modeSwitchPoint = 0
            stepCountAtSwitch = stepCount
        }

        // Check if we should switch modes
        val shouldSwitchToStepBased = shouldUseStepBased(route, stepCount)
        val shouldSwitchToGPS = shouldUseGPS(route, stepCount)

        // Handle mode switching
        if (currentMode == CalculationMode.GPS && shouldSwitchToStepBased) {
            currentMode = CalculationMode.STEP_BASED
            modeSwitchPoint = route.size - 1
            stepCountAtSwitch = stepCount
            Log.d("DistanceCalculator", "Switched to STEP_BASED mode at point $modeSwitchPoint")
        } else if (currentMode == CalculationMode.STEP_BASED && shouldSwitchToGPS) {
            currentMode = CalculationMode.GPS
            modeSwitchPoint = route.size - 1
            stepCountAtSwitch = stepCount
            Log.d("DistanceCalculator" ,"Switched to GPS mode at point $modeSwitchPoint")
        }

        // Calculate distance based on current mode
        return when (currentMode) {
            CalculationMode.GPS -> calculateGpsDistance(route)
            CalculationMode.STEP_BASED -> calculateStepBasedDistance(stepCount)
        }
    }

    /**
     * Simple check: use step-based if GPS shows little movement but user took many steps
     */
    private fun shouldUseStepBased(route: List<TrackingPoint>, stepCount: Long): Boolean {
        if (stepCount < 20) return false // Need minimum steps

        val gpsDistance = calculateGpsDistance(route)
        val stepBasedDistance = stepCount * AVERAGE_STRIDE_LENGTH

        // Switch if steps suggest much more distance than GPS (indoor walking)
        return stepBasedDistance > gpsDistance * 1.8 && stepCount > 25
    }

    /**
     * Switch back to GPS if we have good GPS signal and reasonable speed
     */
    private fun shouldUseGPS(route: List<TrackingPoint>, stepCount: Long): Boolean {
        if (route.size < 5) return false

        // Check last few points for good GPS data
        val recentPoints = route.takeLast(5)
        var goodGpsCount = 0

        for (i in 1 until recentPoints.size) {
            val timeDiff = abs(recentPoints[i].timestampEpochMillis - recentPoints[i-1].timestampEpochMillis)
            if (timeDiff in 1000L..10000L) { // Reasonable time gaps
                goodGpsCount++
            }
        }

        return goodGpsCount >= 3 // Most recent points have good GPS
    }

    /**
     * GPS distance calculation with speed filtering
     */
    private fun calculateGpsDistance(route: List<TrackingPoint>): Double {
        var totalDistance = 0.0
        var previousPoint: TrackingPoint? = null

        for (currentPoint in route) {
            if (previousPoint == null) {
                previousPoint = currentPoint
                continue
            }

            val timeDiff = abs(currentPoint.timestampEpochMillis - previousPoint.timestampEpochMillis)
            if (timeDiff > MAX_TIME_DIFF_MS || timeDiff == 0L) continue

            val distance = calculateDistance(previousPoint, currentPoint)
            val speed = distance / (timeDiff / 1000.0)

            if (speed in MIN_WALKING_SPEED..MAX_WALKING_SPEED) {
                totalDistance += distance
                previousPoint = currentPoint
            }
        }
        return totalDistance
    }

    /**
     * Step-based distance
     */
    private fun calculateStepBasedDistance(stepCount: Long): Double {
        return stepCount * AVERAGE_STRIDE_LENGTH
    }

    /**
     * Reset calculator state for new session
     */
    fun reset() {
        currentMode = CalculationMode.GPS
        modeSwitchPoint = 0
        stepCountAtSwitch = 0L
    }
}