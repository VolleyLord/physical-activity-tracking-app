package com.volleylord.gps_tracker.data.remote.dto

import com.google.firebase.firestore.PropertyName

/**
 * Firestore DTO for TrackingPoint.
 * Maps domain TrackingPoint to Firestore-compatible structure.
 */
data class TrackingPointDto(
    @get:PropertyName("latitude")
    @set:PropertyName("latitude")
    var latitude: Double = 0.0,

    @get:PropertyName("longitude")
    @set:PropertyName("longitude")
    var longitude: Double = 0.0,

    @get:PropertyName("altitude_meters")
    @set:PropertyName("altitude_meters")
    var altitudeMeters: Double? = null,

    @get:PropertyName("timestamp_epoch_millis")
    @set:PropertyName("timestamp_epoch_millis")
    var timestampEpochMillis: Long = 0L,

    @get:PropertyName("elapsed_time_millis")
    @set:PropertyName("elapsed_time_millis")
    var elapsedTimeMillis: Long = 0L
)

