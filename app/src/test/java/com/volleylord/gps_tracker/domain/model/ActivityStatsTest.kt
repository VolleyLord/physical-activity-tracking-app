package com.volleylord.gps_tracker.domain.model

import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ActivityStatsTest {

    @Test
    fun `creates stats when values are valid`() {
        val stats = ActivityStats(
            distanceMeters = 1500.0,
            stepCount = 2000,
            elapsed = 12.5.minutes
        )

        assertEquals(1500.0, stats.distanceMeters, 0.0)
        assertEquals(2000, stats.stepCount)
    }

    @Test
    fun `throws when distance is negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            ActivityStats(
                distanceMeters = -1.0,
                stepCount = 100,
                elapsed = 1.minutes
            )
        }
    }

    @Test
    fun `throws when steps are negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            ActivityStats(
                distanceMeters = 1.0,
                stepCount = -1,
                elapsed = 1.minutes
            )
        }
    }
}

