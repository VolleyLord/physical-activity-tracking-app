package com.volleylord.gps_tracker.data.repository

import com.volleylord.gps_tracker.data.remote.api.FirestoreActivitySessionDataSource
import com.volleylord.gps_tracker.data.util.DistanceCalculator
import com.volleylord.gps_tracker.data.util.LocationTracker
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.model.ActivityStats
import com.volleylord.gps_tracker.domain.model.ActivityStatus
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ActivitySessionRepository implementation using Firestore.
 * Manages active session state and coordinates with LocationTracker.
 */
@Singleton
class ActivitySessionRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreActivitySessionDataSource,
    private val locationTracker: LocationTracker,
    private val authRepository: AuthRepository
) : ActivitySessionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // In-memory state for active session
    private val _activeSessionId = MutableStateFlow<String?>(null)
    private val _route = MutableStateFlow<List<TrackingPoint>>(emptyList())
    private val sessionStartTime = MutableStateFlow<Long?>(null)
    private var locationTrackingJob: kotlinx.coroutines.Job? = null

    override fun observeActiveSession(): Flow<ActivitySession?> {
        return combine(
            authRepository.currentUser,
            _activeSessionId,
            _route,
            locationTracker.stepCount,
            sessionStartTime
        ) { user, sessionId, route, stepCount, startTime ->
            if (user == null || sessionId == null || startTime == null) return@combine null

            val elapsed = (System.currentTimeMillis() - startTime).milliseconds
            val distance = DistanceCalculator.calculateTotalDistance(route)

            ActivitySession(
                id = sessionId,
                userId = user.uid,
                startedAtEpochMillis = startTime,
                endedAtEpochMillis = null,
                route = route,
                stats = ActivityStats(
                    distanceMeters = distance,
                    stepCount = stepCount,
                    elapsed = elapsed
                ),
                status = ActivityStatus.Active
            )
        }
    }

    override suspend fun startNewSession(): ActivitySession {
        val user = authRepository.currentUser.first()
            ?: throw IllegalStateException("User must be authenticated to start session")

        val startTime = System.currentTimeMillis()
        sessionStartTime.value = startTime
        locationTracker.resetStepCount()

        // Create initial session data - we'll create the domain object after getting Firestore ID
        // First, create the session in Firestore to get the ID
        val tempSession = ActivitySession(
            id = "temp_${System.currentTimeMillis()}", // Temporary ID to pass validation
            userId = user.uid,
            startedAtEpochMillis = startTime,
            endedAtEpochMillis = null,
            route = emptyList(),
            stats = ActivityStats(
                distanceMeters = 0.0,
                stepCount = 0L,
                elapsed = Duration.ZERO
            ),
            status = ActivityStatus.Active
        )

        val sessionId = firestoreDataSource.createSession(tempSession)
        _activeSessionId.value = sessionId
        _route.value = emptyList()

        // Create the actual domain object with the real Firestore ID
        val initialSession = tempSession.copy(id = sessionId)

        // Start location tracking in background
        locationTrackingJob?.cancel()
        locationTrackingJob = scope.launch {
            locationTracker.startTracking(startTime).collect { point ->
                _route.update { it + point }
                // Periodically update Firestore (every 10 points to reduce writes)
                if (_route.value.size % 10 == 0) {
                    updateSessionInFirestore()
                }
            }
        }

        return initialSession
    }

    override suspend fun stopSession(sessionId: String): ActivitySession {
        locationTrackingJob?.cancel()
        locationTracker.stopTracking()

        val user = authRepository.currentUser.first()
            ?: throw IllegalStateException("User must be authenticated")

        val route = _route.value
        val stepCount = locationTracker.stepCount.value
        val startTime = sessionStartTime.value ?: System.currentTimeMillis()
        val endTime = System.currentTimeMillis()
        val elapsed = (endTime - startTime).milliseconds
        val distance = DistanceCalculator.calculateTotalDistance(route)

        val completedSession = ActivitySession(
            id = sessionId,
            userId = user.uid,
            startedAtEpochMillis = startTime,
            endedAtEpochMillis = endTime,
            route = route,
            stats = ActivityStats(
                distanceMeters = distance,
                stepCount = stepCount,
                elapsed = elapsed
            ),
            status = ActivityStatus.Completed
        )

        firestoreDataSource.updateSession(sessionId, completedSession)

        _activeSessionId.value = null
        _route.value = emptyList()
        sessionStartTime.value = null

        return completedSession
    }

    override suspend fun appendPoint(sessionId: String, point: TrackingPoint) {
        _route.update { it + point }
        // Update Firestore periodically
        if (_route.value.size % 10 == 0) {
            updateSessionInFirestore()
        }
    }

    override fun observeHistory(limit: Int): Flow<List<ActivitySession>> {
        return authRepository.currentUser.flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else firestoreDataSource.observeHistory(user.uid, limit)
        }
    }

    private suspend fun updateSessionInFirestore() {
        val sessionId = _activeSessionId.value ?: return
        val user = authRepository.currentUser.first() ?: return
        val route = _route.value
        val stepCount = locationTracker.stepCount.value
        val startTime = sessionStartTime.value ?: return

        val elapsed = (System.currentTimeMillis() - startTime).milliseconds
        val distance = DistanceCalculator.calculateTotalDistance(route)

        val session = ActivitySession(
            id = sessionId,
            userId = user.uid,
            startedAtEpochMillis = startTime,
            endedAtEpochMillis = null,
            route = route,
            stats = ActivityStats(
                distanceMeters = distance,
                stepCount = stepCount,
                elapsed = elapsed
            ),
            status = ActivityStatus.Active
        )

        firestoreDataSource.updateSession(sessionId, session)
    }
}

