package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.UpdateSessionNotesUseCase
import javax.inject.Inject

class UpdateSessionNotesUseCaseImpl @Inject constructor(
    private val repository: ActivitySessionRepository
) : UpdateSessionNotesUseCase {
    override suspend fun invoke(sessionId: String, notes: String) {
        repository.updateSessionNotes(sessionId, notes)
    }
}

