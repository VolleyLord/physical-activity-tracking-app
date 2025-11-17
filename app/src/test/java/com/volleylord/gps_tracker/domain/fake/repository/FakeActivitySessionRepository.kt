package com.volleylord.gps_tracker.domain.fake.repository

import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.model.ActivityStats
import com.volleylord.gps_tracker.domain.model.ActivityStatus
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class FakeActivitySessionRepository : ActivitySessionRepository {

    private val activeSessionState = MutableStateFlow<ActivitySession?>(null)
    private val historyState = MutableStateFlow<List<ActivitySession>>(emptyList())
    private val _isPaused = MutableStateFlow(false)
    override val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    override fun observeActiveSession(): Flow<ActivitySession?> = activeSessionState.asStateFlow()

    override suspend fun startNewSession(): ActivitySession {
        _isPaused.value = false
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
        _isPaused.value = false
        historyState.update { listOf(completed) + it }
        return completed
    }

    override suspend fun pauseSession() {
        val active = activeSessionState.value ?: return
        _isPaused.value = true
        activeSessionState.value = active.copy(
            status = ActivityStatus.Paused
        )
    }

    override suspend fun resumeSession() {
        val active = activeSessionState.value ?: return
        _isPaused.value = false
        activeSessionState.value = active.copy(
            status = ActivityStatus.Active
        )
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