package com.volleylord.gps_tracker.data.remote.mapper

import com.volleylord.gps_tracker.data.remote.dto.TrackingPointDto
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Maps between domain TrackingPoint and Firestore DTO.
 */
object TrackingPointMapper {

    fun domainToDto(point: TrackingPoint): TrackingPointDto = TrackingPointDto(
        latitude = point.latitude,
        longitude = point.longitude,
        altitudeMeters = point.altitudeMeters,
        timestampEpochMillis = point.timestampEpochMillis,
        elapsedTimeMillis = point.elapsedTime.inWholeMilliseconds
    )

    fun dtoToDomain(dto: TrackingPointDto): TrackingPoint = TrackingPoint(
        latitude = dto.latitude,
        longitude = dto.longitude,
        altitudeMeters = dto.altitudeMeters,
        timestampEpochMillis = dto.timestampEpochMillis,
        elapsedTime = dto.elapsedTimeMillis.milliseconds
    )
}

