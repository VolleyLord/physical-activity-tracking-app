package com.volleylord.gps_tracker.domain.usecase

fun interface UpdateSessionNotesUseCase {
    suspend operator fun invoke(sessionId: String, notes: String)
}

