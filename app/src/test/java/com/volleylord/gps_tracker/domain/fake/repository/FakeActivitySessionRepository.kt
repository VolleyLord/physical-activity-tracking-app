package com.volleylord.gps_tracker.domain.fake.repository

import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.model.ActivityStats
import com.volleylord.gps_tracker.domain.model.ActivityStatus
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class FakeActivitySessionRepository : ActivitySessionRepository {

    private val activeSessionState = MutableStateFlow<ActivitySession?>(null)
    private val historyState = MutableStateFlow<List<ActivitySession>>(emptyList())

    override fun observeActiveSession(): Flow<ActivitySession?> = activeSessionState.asStateFlow()

    override suspend fun startNewSession(): ActivitySession {
        val session = ActivitySession(
            id = UUID.randomUUID().toString(),
            userId = "fake-user",
            startedAtEpochMillis = System.currentTimeMillis(),
            endedAtEpochMillis = null,
            route = listOf(
                TrackingPoint(
                    latitude = 37.4219999,
                    longitude = -122.0840575,
                    altitudeMeters = 5.0,
                    timestampEpochMillis = System.currentTimeMillis(),
                    elapsedTime = Duration.Companion.ZERO
                )
            ),
            stats = ActivityStats(
                distanceMeters = 0.0,
                stepCount = 0,
                elapsed = Duration.Companion.ZERO
            ),
            status = ActivityStatus.Active
        )
        activeSessionState.value = session
        return session
    }

    override suspend fun stopSession(sessionId: String): ActivitySession {
        val active = activeSessionState.value
        require(active?.id == sessionId) { "No active session with id $sessionId" }
        val completed = active.copy(
            endedAtEpochMillis = System.currentTimeMillis(),
            stats = active.stats.copy(
                distanceMeters = 1200.0,
                stepCount = 1534,
                elapsed = 15.minutes
            ),
            status = ActivityStatus.Completed
        )
        activeSessionState.value = null
        historyState.update { listOf(completed) + it }
        return completed
    }

    override suspend fun appendPoint(sessionId: String, point: TrackingPoint) {
        val active = activeSessionState.value ?: return
        if (active.id != sessionId) return
        activeSessionState.value = active.copy(
            route = active.route + point
        )
    }

    override fun observeHistory(limit: Int): Flow<List<ActivitySession>> =
        historyState.asStateFlow()
            .map { sessions -> sessions.take(limit) }
}