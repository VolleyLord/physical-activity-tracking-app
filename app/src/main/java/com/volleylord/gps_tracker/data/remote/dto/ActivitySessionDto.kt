package com.volleylord.gps_tracker.data.remote.dto

import com.google.firebase.firestore.PropertyName

/**
 * Firestore DTO for ActivitySession.
 * Maps domain ActivitySession to Firestore document structure.
 */
data class ActivitySessionDto(
    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("started_at_epoch_millis")
    @set:PropertyName("started_at_epoch_millis")
    var startedAtEpochMillis: Long = 0L,

    @get:PropertyName("ended_at_epoch_millis")
    @set:PropertyName("ended_at_epoch_millis")
    var endedAtEpochMillis: Long? = null,

    @get:PropertyName("route")
    @set:PropertyName("route")
    var route: List<TrackingPointDto> = emptyList(),

    @get:PropertyName("stats")
    @set:PropertyName("stats")
    var stats: ActivityStatsDto = ActivityStatsDto(),

    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "active", // "active", "completed", "aborted"

    @get:PropertyName("notes")
    @set:PropertyName("notes")
    var notes: String? = null
)

