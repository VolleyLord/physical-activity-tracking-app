package com.volleylord.gps_tracker.data.remote.mapper

import com.volleylord.gps_tracker.data.remote.dto.ActivityStatsDto
import com.volleylord.gps_tracker.domain.model.ActivityStats
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Maps between domain ActivityStats and Firestore DTO.
 */
object ActivityStatsMapper {

    fun domainToDto(stats: ActivityStats): ActivityStatsDto = ActivityStatsDto(
        distanceMeters = stats.distanceMeters,
        stepCount = stats.stepCount,
        elapsedMillis = stats.elapsed.inWholeMilliseconds
    )

    fun dtoToDomain(dto: ActivityStatsDto): ActivityStats = ActivityStats(
        distanceMeters = dto.distanceMeters,
        stepCount = dto.stepCount,
        elapsed = dto.elapsedMillis.milliseconds
    )
}

