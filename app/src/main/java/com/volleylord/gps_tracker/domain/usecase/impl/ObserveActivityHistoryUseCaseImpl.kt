package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.model.ActivitySession
import com.volleylord.gps_tracker.domain.repository.ActivitySessionRepository
import com.volleylord.gps_tracker.domain.usecase.ObserveActivityHistoryUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveActivityHistoryUseCaseImpl @Inject constructor(
    private val repository: ActivitySessionRepository
) : ObserveActivityHistoryUseCase {
    override fun invoke(limit: Int): Flow<List<ActivitySession>> =
        repository.observeHistory(limit)
}

