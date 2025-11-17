package com.volleylord.gps_tracker.domain.usecase.impl

import android.content.Context
import com.volleylord.gps_tracker.data.service.TrackingForegroundService
import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.StartActivityTrackingUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for starting activity tracking.
 * Coordinates repository session creation and foreground service for background tracking.
 */
class StartActivityTrackingUseCaseImpl @Inject constructor(
    private val repository: ActivitySessionRepository,
    @ApplicationContext private val context: Context
) : StartActivityTrackingUseCase {
    override suspend fun invoke(): Result<ActivitySession> = runCatching {
        // Start foreground service for background location tracking
        TrackingForegroundService.start(context)

        // Add a small delay to ensure service is started
        kotlinx.coroutines.delay(100)
        
        // Create new session in repository (this also starts location tracking)
        val session = repository.startNewSession()
        
        session
    }
}

