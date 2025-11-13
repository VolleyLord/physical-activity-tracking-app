package com.volleylord.gps_tracker.domain.usecase

import com.volleylord.gps_tracker.domain.model.ActivitySession

fun interface StopActivityTrackingUseCase {
    suspend operator fun invoke(sessionId: String): Result<ActivitySession>
}

