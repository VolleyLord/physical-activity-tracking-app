package com.volleylord.gps_tracker.data.util

import android.location.Location
import com.volleylord.gps_tracker.domain.model.TrackingPoint

/**
 * Utility for calculating distance between tracking points.
 * Uses Haversine formula for great-circle distance.
 */
object DistanceCalculator {

    /**
     * Calculates distance in meters between two tracking points.
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
     * Calculates total distance for a route.
     */
    fun calculateTotalDistance(route: List<TrackingPoint>): Double {
        if (route.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 1 until route.size) {
            totalDistance += calculateDistance(route[i - 1], route[i])
        }
        return totalDistance
    }
}

