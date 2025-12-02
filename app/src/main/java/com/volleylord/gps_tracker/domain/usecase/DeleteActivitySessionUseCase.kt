package com.volleylord.gps_tracker.domain.usecase

fun interface DeleteActivitySessionUseCase {
    suspend operator fun invoke(sessionId: String): Result<Unit>
}

