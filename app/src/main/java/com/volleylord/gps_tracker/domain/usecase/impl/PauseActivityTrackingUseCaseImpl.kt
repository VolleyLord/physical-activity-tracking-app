package com.volleylord.gps_tracker.domain.usecase.impl

import android.content.Context
import com.volleylord.gps_tracker.data.service.TrackingForegroundService
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.PauseActivityTrackingUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for pausing activity tracking.
 * Pauses repository session and updates foreground service notification.
 */
class PauseActivityTrackingUseCaseImpl @Inject constructor(
    private val repository: ActivitySessionRepository,
    @ApplicationContext private val context: Context
) : PauseActivityTrackingUseCase {
    override suspend fun invoke() {
        repository.pauseSession()
        TrackingForegroundService.pause(context)
    }
}
