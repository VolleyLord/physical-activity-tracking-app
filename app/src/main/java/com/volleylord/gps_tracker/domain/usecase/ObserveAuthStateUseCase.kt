package com.volleylord.gps_tracker.domain.usecase

import com.volleylord.gps_tracker.domain.model.User
import kotlinx.coroutines.flow.Flow

fun interface ObserveAuthStateUseCase {
    operator fun invoke(): Flow<User?>
}

