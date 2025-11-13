package com.volleylord.gps_tracker.domain.model

import kotlin.time.Duration

data class ActivitySession(
    val id: String,
    val userId: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?,
    val route: List<TrackingPoint>,
    val stats: ActivityStats,
    val status: ActivityStatus
) {
    init {
        require(id.isNotBlank()) { "id cannot be blank" }
        require(userId.isNotBlank()) { "userId cannot be blank" }
        require(startedAtEpochMillis > 0) { "startedAtEpochMillis must be > 0" }
        require(
            endedAtEpochMillis == null || endedAtEpochMillis >= startedAtEpochMillis
        ) { "endedAtEpochMillis must be >= start when provided" }
        require(route.isNotEmpty()) { "route cannot be empty" }
        if (status == ActivityStatus.Completed) {
            requireNotNull(endedAtEpochMillis) { "completed session must have end time" }
        }
    }
}

sealed interface ActivityStatus {
    data object Active : ActivityStatus
    data object Completed : ActivityStatus
    data class Aborted(val reason: String?) : ActivityStatus
}

