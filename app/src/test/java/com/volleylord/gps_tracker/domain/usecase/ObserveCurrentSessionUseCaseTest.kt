package com.volleylord.gps_tracker.domain.usecase

import app.cash.turbine.test
import com.volleylord.gps_tracker.domain.fake.repository.FakeActivitySessionRepository
import com.volleylord.gps_tracker.domain.model.ActivityStatus
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import com.volleylord.gps_tracker.domain.usecase.impl.ObserveCurrentSessionUseCaseImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * Unit tests for ObserveCurrentSessionUseCase using Turbine for Flow testing.
 * Validates real-time session state updates, stats accumulation, and state transitions.
 */
class ObserveCurrentSessionUseCaseTest {

    private lateinit var repository: FakeActivitySessionRepository
    private lateinit var useCase: ObserveCurrentSessionUseCase

    @Before
    fun setUp() {
        repository = FakeActivitySessionRepository()
        useCase = ObserveCurrentSessionUseCaseImpl(repository)
    }

    @Test
    fun `observe session emits null when no active session`() = runTest {
        // When
        useCase().test {
            // Then
            val session = awaitItem()
            assertNull(session)
        }
    }

    @Test
    fun `observe session emits active session after start`() = runTest {
        // Given
        val startedSession = repository.startNewSession()

        // When
        useCase().test {
            // Then
            val session = awaitItem()
            assertNotNull(session)
            assertEquals(startedSession.id, session?.id)
            assertEquals(ActivityStatus.Active, session?.status)
        }
    }

    @Test
    fun `observe session updates route as points are added`() = runTest {
        // Given
        val startedSession = repository.startNewSession()
        val sessionId = startedSession.id

        // When
        useCase().test {
            // Initial session
            val initialSession = awaitItem()
            assertNotNull(initialSession)
            val initialRouteSize = initialSession?.route?.size ?: 0

            // Add tracking points
            val point1 = TrackingPoint(
                latitude = 55.2024831,
                longitude = 30.2660463,
                altitudeMeters = 195.5,
                timestampEpochMillis = System.currentTimeMillis(),
                elapsedTime = 1000.milliseconds
            )
            val point2 = TrackingPoint(
                latitude = 55.2025000,
                longitude = 30.2661000,
                altitudeMeters = 195.6,
                timestampEpochMillis = System.currentTimeMillis() + 2000,
                elapsedTime = 3000.milliseconds
            )

            // Add first point and wait for emission
            repository.appendPoint(sessionId, point1)
            val updatedSession1 = awaitItem()
            assertNotNull(updatedSession1)
            assertTrue((updatedSession1?.route?.size ?: 0) > initialRouteSize)

            // Add second point and wait for emission
            repository.appendPoint(sessionId, point2)
            val updatedSession2 = awaitItem()
            assertNotNull(updatedSession2)
            // Route should have even more points now
            assertTrue((updatedSession2?.route?.size ?: 0) > (updatedSession1?.route?.size ?: 0))
        }
    }

    @Test
    fun `observe session emits null after stop`() = runTest {
        // Given
        val startedSession = repository.startNewSession()
        val sessionId = startedSession.id

        // When
        useCase().test {
            // Initially active
            val activeSession = awaitItem()
            assertNotNull(activeSession)

            // Stop session
            repository.stopSession(sessionId)

            // Should emit null
            val stoppedSession = awaitItem()
            assertNull(stoppedSession)
        }
    }

    @Test
    fun `observe session provides session state as flow`() = runTest {
        // Given
        val startedSession = repository.startNewSession()

        // When
        useCase().test {
            // Should emit session state immediately
            val session = awaitItem()
            assertNotNull(session)
            assertEquals(startedSession.id, session?.id)
            assertEquals(ActivityStatus.Active, session?.status)
            
            // Flow should provide real-time session state
            // In real implementation, this would update as location/step data changes
            assertTrue(session?.stats?.elapsed?.inWholeMilliseconds ?: 0 >= 0)
        }
    }
}

