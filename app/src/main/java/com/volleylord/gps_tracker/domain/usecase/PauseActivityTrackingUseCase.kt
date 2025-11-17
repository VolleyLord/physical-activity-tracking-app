package com.volleylord.gps_tracker.domain.usecase

fun interface PauseActivityTrackingUseCase {
    suspend operator fun invoke()
}
