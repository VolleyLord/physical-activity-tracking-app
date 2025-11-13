package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.StopActivityTrackingUseCase
import javax.inject.Inject

class StopActivityTrackingUseCaseImpl @Inject constructor(
    private val repository: ActivitySessionRepository
) : StopActivityTrackingUseCase {
    override suspend fun invoke(sessionId: String): Result<ActivitySession> = runCatching {
        repository.stopSession(sessionId)
    }
}

