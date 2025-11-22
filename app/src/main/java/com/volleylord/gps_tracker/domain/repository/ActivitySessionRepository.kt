package com.volleylord.gps_tracker.domain.repository

import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ActivitySessionRepository {
    fun observeActiveSession(): Flow<ActivitySession?>
    suspend fun startNewSession(): ActivitySession
    suspend fun stopSession(sessionId: String): ActivitySession
    suspend fun pauseSession()
    suspend fun resumeSession()
    suspend fun appendPoint(sessionId: String, point: TrackingPoint): Unit
    fun observeHistory(limit: Int): Flow<List<ActivitySession>>
    fun observeSession(sessionId: String): Flow<ActivitySession?>
    suspend fun updateSessionNotes(sessionId: String, notes: String)
    val isPaused: StateFlow<Boolean>
}

