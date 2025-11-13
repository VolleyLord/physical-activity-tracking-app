package com.volleylord.gps_tracker.domain.repository

import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.model.TrackingPoint
import kotlinx.coroutines.flow.Flow

interface ActivitySessionRepository {
    fun observeActiveSession(): Flow<ActivitySession?>
    suspend fun startNewSession(): ActivitySession
    suspend fun stopSession(sessionId: String): ActivitySession
    suspend fun appendPoint(sessionId: String, point: TrackingPoint): Unit
    fun observeHistory(limit: Int): Flow<List<ActivitySession>>
}

