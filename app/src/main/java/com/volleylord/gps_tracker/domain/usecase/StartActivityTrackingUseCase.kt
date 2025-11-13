package com.volleylord.gps_tracker.domain.usecase

import com.volleylord.gps_tracker.domain.model.ActivitySession

fun interface StartActivityTrackingUseCase {
    suspend operator fun invoke(): Result<ActivitySession>
}

