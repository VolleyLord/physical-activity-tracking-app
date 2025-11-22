package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.model.User
import com.volleylord.gps_tracker.domain.repository.AuthRepository
import com.volleylord.gps_tracker.domain.usecase.SignUpUseCase
import javax.inject.Inject

class SignUpUseCaseImpl @Inject constructor(
    private val authRepository: AuthRepository
) : SignUpUseCase {
    override suspend fun invoke(email: String, password: String): Result<User> =
        authRepository.signUpWithEmail(email, password)
}

