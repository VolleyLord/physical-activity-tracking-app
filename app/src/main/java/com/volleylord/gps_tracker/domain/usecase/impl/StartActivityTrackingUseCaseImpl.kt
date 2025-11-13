package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.StartActivityTrackingUseCase
import javax.inject.Inject

class StartActivityTrackingUseCaseImpl @Inject constructor(
    private val repository: ActivitySessionRepository
) : StartActivityTrackingUseCase {
    override suspend fun invoke(): Result<ActivitySession> = runCatching {
        repository.startNewSession()
    }
}

