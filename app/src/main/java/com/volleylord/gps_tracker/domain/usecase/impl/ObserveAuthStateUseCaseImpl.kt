package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.model.User
import com.volleylord.gps_tracker.domain.repository.AuthRepository
import com.volleylord.gps_tracker.domain.usecase.ObserveAuthStateUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveAuthStateUseCaseImpl @Inject constructor(
    private val authRepository: AuthRepository
) : ObserveAuthStateUseCase {
    override fun invoke(): Flow<User?> = authRepository.currentUser
}

