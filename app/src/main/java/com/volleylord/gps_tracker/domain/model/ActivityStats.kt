package com.volleylord.gps_tracker.domain.model

import kotlin.time.Duration

data class ActivityStats(
    val distanceMeters: Double,
    val stepCount: Long,
    val elapsed: Duration
) {
    init {
        require(distanceMeters >= 0) { "distanceMeters must be >= 0" }
        require(stepCount >= 0) { "stepCount must be >= 0" }
        require(!elapsed.isNegative()) { "elapsed must be >= 0" }
    }
}

