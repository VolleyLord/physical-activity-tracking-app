package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.repository.AuthRepository
import com.volleylord.gps_tracker.domain.usecase.SignOutUseCase
import javax.inject.Inject

class SignOutUseCaseImpl @Inject constructor(
    private val authRepository: AuthRepository
) : SignOutUseCase {
    override suspend fun invoke() {
        authRepository.signOut()
    }
}

