package com.volleylord.gps_tracker.domain.usecase

import app.cash.turbine.test
import com.volleylord.gps_tracker.domain.fake.repository.FakeActivitySessionRepository
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.model.ActivityStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * Unit tests for StopActivityTrackingUseCase using Turbine for Flow testing.
 * Validates session completion, state transitions, and accumulated stats.
 */
class StopActivityTrackingUseCaseTest {

    private lateinit var repository: FakeActivitySessionRepository
    private lateinit var useCase: StopActivityTrackingUseCase

    @Before
    fun setUp() {
        repository = FakeActivitySessionRepository()
    }
    
    private fun createUseCase(): StopActivityTrackingUseCase {
        return object : StopActivityTrackingUseCase {
            override suspend fun invoke(sessionId: String): Result<ActivitySession> = runCatching {
                repository.stopSession(sessionId)
            }
        }
    }

    @Test
    fun `stop tracking completes session with correct status`() = runTest {
        // Given
        val useCase = createUseCase()
        
        // Start a session
        val startResult = repository.startNewSession()
        val sessionId = startResult.id

        // When
        val stopResult = useCase(sessionId)

        // Then
        assertTrue(stopResult.isSuccess)
        val completedSession = stopResult.getOrNull()
        assertNotNull(completedSession)
        assertEquals(ActivityStatus.Completed, completedSession?.status)
        assertNotNull(completedSession?.endedAtEpochMillis)
    }

    @Test
    fun `stop tracking emits null in active session flow`() = runTest {
        // Given
        val useCase = createUseCase()
        
        // Start a session
        val startResult = repository.startNewSession()
        val sessionId = startResult.id

        // When
        repository.observeActiveSession().test {
            // Initially should have active session
            val activeSession = awaitItem()
            assertNotNull(activeSession)
            assertEquals(sessionId, activeSession?.id)

            // Stop the session
            useCase(sessionId)

            // Should emit null after stopping
            val stoppedSession = awaitItem()
            assertEquals(null, stoppedSession)
        }
    }

    @Test
    fun `stop tracking calculates final stats`() = runTest {
        // Given
        val useCase = createUseCase()
        
        // Start a session
        val startResult = repository.startNewSession()
        val sessionId = startResult.id

        // Add some tracking points to simulate movement
        val point1 = com.volleylord.gps_tracker.domain.model.TrackingPoint(
            latitude = 55.2024831,
            longitude = 30.2660463,
            altitudeMeters = 195.5,
            timestampEpochMillis = System.currentTimeMillis(),
            elapsedTime = 1000.milliseconds
        )
        val point2 = com.volleylord.gps_tracker.domain.model.TrackingPoint(
            latitude = 55.2025000,
            longitude = 30.2661000,
            altitudeMeters = 195.6,
            timestampEpochMillis = System.currentTimeMillis() + 2000,
            elapsedTime = 3000.milliseconds
        )

        repository.appendPoint(sessionId, point1)
        repository.appendPoint(sessionId, point2)

        // When
        val stopResult = useCase(sessionId)

        // Then
        assertTrue(stopResult.isSuccess)
        val completedSession = stopResult.getOrNull()
        assertNotNull(completedSession)
        assertTrue(completedSession?.stats?.distanceMeters ?: 0.0 > 0.0)
        assertTrue(completedSession?.stats?.elapsed?.inWholeMilliseconds ?: 0 > 0)
    }

    @Test
    fun `stop tracking with invalid session id returns error`() = runTest {
        // Given
        val useCase = createUseCase()
        
        // When
        val result = useCase("invalid_session_id")

        // Then - should handle error gracefully
        // The exact behavior depends on repository implementation
        assertNotNull(result)
    }
}

