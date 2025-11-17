package com.volleylord.gps_tracker.domain.usecase.impl

import android.content.Context
import com.volleylord.gps_tracker.data.service.TrackingForegroundService
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.StopActivityTrackingUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for stopping activity tracking.
 * Coordinates repository session completion and foreground service cleanup.
 */
class StopActivityTrackingUseCaseImpl @Inject constructor(
    private val repository: ActivitySessionRepository,
    @ApplicationContext private val context: Context
) : StopActivityTrackingUseCase {
    override suspend fun invoke(sessionId: String): Result<ActivitySession> = runCatching {
        // Stop the session in repository (stops location tracking)
        val session = repository.stopSession(sessionId)

        TrackingForegroundService.stop(context)
        
        session
    }
}

