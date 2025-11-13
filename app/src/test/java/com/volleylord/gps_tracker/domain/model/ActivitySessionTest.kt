package com.volleylord.gps_tracker.domain.model

import kotlin.time.Duration
import org.junit.Assert.assertThrows
import org.junit.Test

class ActivitySessionTest {

    private fun samplePoint(): TrackingPoint =
        TrackingPoint(
            latitude = 0.0,
            longitude = 0.0,
            altitudeMeters = null,
            timestampEpochMillis = 1_000L,
            elapsedTime = Duration.ZERO
        )

    private fun sampleStats(): ActivityStats =
        ActivityStats(
            distanceMeters = 10.0,
            stepCount = 20,
            elapsed = Duration.ZERO
        )

    @Test
    fun `throws when route is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            ActivitySession(
                id = "id",
                userId = "user",
                startedAtEpochMillis = 1L,
                endedAtEpochMillis = null,
                route = emptyList(),
                stats = sampleStats(),
                status = ActivityStatus.Active
            )
        }
    }

    @Test
    fun `throws when completed session missing end time`() {
        assertThrows(IllegalArgumentException::class.java) {
            ActivitySession(
                id = "id",
                userId = "user",
                startedAtEpochMillis = 1L,
                endedAtEpochMillis = null,
                route = listOf(samplePoint()),
                stats = sampleStats(),
                status = ActivityStatus.Completed
            )
        }
    }
}

