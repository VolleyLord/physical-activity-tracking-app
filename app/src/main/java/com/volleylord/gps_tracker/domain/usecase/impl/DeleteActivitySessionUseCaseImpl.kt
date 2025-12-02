package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.DeleteActivitySessionUseCase
import javax.inject.Inject

/**
 * Use case for deleting an activity session.
 */
class DeleteActivitySessionUseCaseImpl @Inject constructor(
    private val repository: ActivitySessionRepository
) : DeleteActivitySessionUseCase {
    override suspend fun invoke(sessionId: String): Result<Unit> = runCatching {
        repository.deleteSession(sessionId)
    }
}

