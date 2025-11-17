package com.volleylord.gps_tracker.domain.usecase.impl

import android.content.Context
import com.volleylord.gps_tracker.data.service.TrackingForegroundService
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.ResumeActivityTrackingUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for resuming activity tracking.
 * Resumes repository session and updates foreground service notification.
 */
class ResumeActivityTrackingUseCaseImpl @Inject constructor(
    private val repository: ActivitySessionRepository,
    @ApplicationContext private val context: Context
) : ResumeActivityTrackingUseCase {
    override suspend fun invoke() {
        repository.resumeSession()
        TrackingForegroundService.resume(context)
    }
}
