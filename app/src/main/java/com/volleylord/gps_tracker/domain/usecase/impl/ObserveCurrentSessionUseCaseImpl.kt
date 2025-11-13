package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.ObserveCurrentSessionUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCurrentSessionUseCaseImpl @Inject constructor(
    private val repository: ActivitySessionRepository
) : ObserveCurrentSessionUseCase {
    override fun invoke(): Flow<ActivitySession?> = repository.observeActiveSession()
}

