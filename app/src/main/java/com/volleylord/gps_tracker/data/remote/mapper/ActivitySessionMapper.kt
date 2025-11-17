package com.volleylord.gps_tracker.data.remote.mapper

import com.volleylord.gps_tracker.data.remote.dto.ActivitySessionDto
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.model.ActivityStatus

/**
 * Maps between domain ActivitySession and Firestore DTO.
 */
object ActivitySessionMapper {

    fun domainToDto(session: ActivitySession): ActivitySessionDto = ActivitySessionDto(
        userId = session.userId,
        startedAtEpochMillis = session.startedAtEpochMillis,
        endedAtEpochMillis = session.endedAtEpochMillis,
        route = session.route.map { TrackingPointMapper.domainToDto(it) },
        stats = ActivityStatsMapper.domainToDto(session.stats),
        status = when (session.status) {
            is ActivityStatus.Active -> "active"
            is ActivityStatus.Paused -> "paused"
            is ActivityStatus.Completed -> "completed"
            is ActivityStatus.Aborted -> "aborted"
        }
    )

    fun dtoToDomain(id: String, dto: ActivitySessionDto): ActivitySession {
        val status = when (dto.status) {
            "active" -> ActivityStatus.Active
            "paused" -> ActivityStatus.Paused
            "completed" -> ActivityStatus.Completed
            "aborted" -> ActivityStatus.Aborted(dto.status)
            else -> ActivityStatus.Active
        }

        return ActivitySession(
            id = id,
            userId = dto.userId,
            startedAtEpochMillis = dto.startedAtEpochMillis,
            endedAtEpochMillis = dto.endedAtEpochMillis,
            route = dto.route.map { TrackingPointMapper.dtoToDomain(it) },
            stats = ActivityStatsMapper.dtoToDomain(dto.stats),
            status = status
        )
    }
}

