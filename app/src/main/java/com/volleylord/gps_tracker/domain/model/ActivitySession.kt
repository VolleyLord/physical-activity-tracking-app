package com.volleylord.gps_tracker.domain.model

data class ActivitySession(
    val id: String,
    val userId: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?,
    val route: List<TrackingPoint>,
    val stats: ActivityStats,
    val status: ActivityStatus,
    val notes: String? = null
) {
    init {
        require(id.isNotBlank()) { "id cannot be blank" }
        require(userId.isNotBlank()) { "userId cannot be blank" }
        require(startedAtEpochMillis > 0) { "startedAtEpochMillis must be > 0" }
        require(
            endedAtEpochMillis == null || endedAtEpochMillis >= startedAtEpochMillis
        ) { "endedAtEpochMillis must be >= start when provided" }
        // Allow empty route for active/paused sessions and completed sessions
        // Route may be empty if tracking was stopped immediately or no GPS signal
        if (status == ActivityStatus.Completed) {
            requireNotNull(endedAtEpochMillis) { "completed session must have end time" }
        }
    }
}

sealed interface ActivityStatus {
    data object Active : ActivityStatus
    data object Paused : ActivityStatus
    data object Completed : ActivityStatus
    data class Aborted(val reason: String?) : ActivityStatus
}

