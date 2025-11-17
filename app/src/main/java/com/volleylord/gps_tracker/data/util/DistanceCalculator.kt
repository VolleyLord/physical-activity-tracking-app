package com.volleylord.gps_tracker.data.util

import android.location.Location
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import kotlin.math.abs
import kotlin.math.max

/**
 * Utility for calculating distance using hybrid approach (GPS + step-based estimation).
 * Combines GPS distance with pedometer-based distance for improved accuracy.
 * Uses Haversine formula for GPS distance and step count × stride length for pedometer distance.
 */
object DistanceCalculator {
    
    // Minimum distance threshold to filter GPS drift (in meters)
    // Points closer than this are considered noise/drift
    private const val MIN_DISTANCE_THRESHOLD = 3.0 // 3 meters
    
    // Maximum time difference between points to consider them valid (in milliseconds)
    // Points with larger time gaps might indicate GPS signal loss
    private const val MAX_TIME_DIFF_MS = 30000L // 30 seconds
    
    // Average stride length in meters (typical adult stride: 0.7-0.8m, using 0.75m)
    private const val AVERAGE_STRIDE_LENGTH_METERS = 0.75
    
    // GPS accuracy threshold - if GPS points are too sparse, rely more on step-based
    private const val MIN_GPS_POINTS_FOR_RELIABILITY = 5
    
    // Maximum GPS distance variance to consider GPS reliable (in meters)
    // If GPS distance varies too much, step-based might be more accurate
    private const val MAX_GPS_VARIANCE_METERS = 50.0

    /**
     * Calculates distance in meters between two tracking points using GPS.
     */
    fun calculateDistance(point1: TrackingPoint, point2: TrackingPoint): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            point1.latitude,
            point1.longitude,
            point2.latitude,
            point2.longitude,
            results
        )
        return results[0].toDouble()
    }

    /**
     * Calculates step-based distance estimation.
     * @param stepCount Number of steps taken
     * @return Estimated distance in meters based on step count and average stride length
     */
    fun calculateStepBasedDistance(stepCount: Long): Double {
        return stepCount * AVERAGE_STRIDE_LENGTH_METERS
    }

    /**
     * Calculates total distance using hybrid approach (GPS + step-based).
     * Combines GPS distance with step-based estimation for improved accuracy.
     * 
     * Strategy:
     * - If GPS has enough reliable points, use weighted average (70% GPS, 30% step-based)
     * - If GPS is unreliable or sparse, use step-based primarily
     * - If both are available, use the maximum (to avoid underestimating)
     * 
     * @param route List of GPS tracking points
     * @param stepCount Number of steps taken during the route
     * @return Total distance in meters using hybrid calculation
     */
    fun calculateTotalDistance(route: List<TrackingPoint>, stepCount: Long = 0L): Double {
        // Calculate GPS-based distance
        val gpsDistance = calculateGpsDistance(route)
        
        // Calculate step-based distance
        val stepBasedDistance = if (stepCount > 0) {
            calculateStepBasedDistance(stepCount)
        } else {
            0.0
        }
        
        // Hybrid approach: combine both methods
        return calculateHybridDistance(gpsDistance, stepBasedDistance, route.size, stepCount)
    }

    /**
     * Calculates GPS-based distance with filtering.
     */
    private fun calculateGpsDistance(route: List<TrackingPoint>): Double {
        if (route.size < 2) return 0.0

        var totalDistance = 0.0
        var lastValidPoint: TrackingPoint? = null
        
        for (i in route.indices) {
            val currentPoint = route[i]
            
            // Skip first point (no previous point to compare)
            if (lastValidPoint == null) {
                lastValidPoint = currentPoint
                continue
            }
            
            // Check time difference to detect GPS signal loss
            val timeDiff = abs(currentPoint.timestampEpochMillis - lastValidPoint.timestampEpochMillis)
            if (timeDiff > MAX_TIME_DIFF_MS) {
                // Large time gap - might indicate GPS signal loss
                // Skip this point but keep lastValidPoint for next comparison
                continue
            }
            
            val distance = calculateDistance(lastValidPoint, currentPoint)
            
            // Filter out GPS drift - only count significant movements
            if (distance >= MIN_DISTANCE_THRESHOLD) {
                totalDistance += distance
                lastValidPoint = currentPoint
            }
            // If distance is too small, keep lastValidPoint but don't add to total
            // This filters out GPS drift when stationary
        }
        
        return totalDistance
    }

    /**
     * Combines GPS and step-based distances using intelligent weighting.
     */
    private fun calculateHybridDistance(
        gpsDistance: Double,
        stepBasedDistance: Double,
        gpsPointCount: Int,
        stepCount: Long
    ): Double {
        // If no steps available, use GPS only
        if (stepCount == 0L) {
            return gpsDistance
        }
        
        // If GPS has very few points, rely more on step-based
        if (gpsPointCount < MIN_GPS_POINTS_FOR_RELIABILITY) {
            // Use step-based primarily, but add GPS if available
            return max(stepBasedDistance, gpsDistance * 0.3)
        }
        
        // If GPS distance is very small but steps suggest movement, use step-based
        if (gpsDistance < 10.0 && stepBasedDistance > 20.0) {
            return stepBasedDistance
        }
        
        // If step-based is significantly larger, GPS might be underestimating
        // (e.g., in areas with poor GPS signal)
        if (stepBasedDistance > gpsDistance * 1.5) {
            // Use weighted average favoring step-based
            return (gpsDistance * 0.3) + (stepBasedDistance * 0.7)
        }
        
        // If GPS is significantly larger, step-based might be underestimating
        // (e.g., running with longer strides)
        if (gpsDistance > stepBasedDistance * 1.5) {
            // Use weighted average favoring GPS
            return (gpsDistance * 0.7) + (stepBasedDistance * 0.3)
        }
        
        // Both are reasonably close - use weighted average
        // GPS is generally more accurate for straight-line distance
        // Step-based is better for actual distance walked (accounts for turns, etc.)
        return (gpsDistance * 0.6) + (stepBasedDistance * 0.4)
    }
}

