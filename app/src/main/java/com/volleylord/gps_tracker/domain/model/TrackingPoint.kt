package com.volleylord.gps_tracker.domain.model

import kotlin.time.Duration

data class TrackingPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val timestampEpochMillis: Long,
    val elapsedTime: Duration
)

