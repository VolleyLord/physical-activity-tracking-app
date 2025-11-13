package com.volleylord.gps_tracker.domain.usecase

import com.volleylord.gps_tracker.domain.model.ActivitySession
import kotlinx.coroutines.flow.Flow

fun interface ObserveActivityHistoryUseCase {
    operator fun invoke(limit: Int): Flow<List<ActivitySession>>
}

