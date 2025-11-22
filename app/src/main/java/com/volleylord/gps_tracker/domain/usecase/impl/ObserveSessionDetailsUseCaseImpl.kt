package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.ObserveSessionDetailsUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSessionDetailsUseCaseImpl @Inject constructor(
    private val repository: ActivitySessionRepository
) : ObserveSessionDetailsUseCase {
    override fun invoke(sessionId: String): Flow<ActivitySession?> =
        repository.observeSession(sessionId)
}

