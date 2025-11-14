package com.volleylord.gps_tracker.data.remote.dto

import com.google.firebase.firestore.PropertyName

/**
 * Firestore DTO for ActivityStats.
 * Maps domain ActivityStats to Firestore-compatible structure.
 */
data class ActivityStatsDto(
    @get:PropertyName("distance_meters")
    @set:PropertyName("distance_meters")
    var distanceMeters: Double = 0.0,

    @get:PropertyName("step_count")
    @set:PropertyName("step_count")
    var stepCount: Long = 0L,

    @get:PropertyName("elapsed_millis")
    @set:PropertyName("elapsed_millis")
    var elapsedMillis: Long = 0L
)

