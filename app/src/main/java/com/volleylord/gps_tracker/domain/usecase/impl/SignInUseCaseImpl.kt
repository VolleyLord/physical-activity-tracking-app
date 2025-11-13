package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.model.User
import com.volleylord.gps_tracker.domain.repository.AuthRepository
import com.volleylord.gps_tracker.domain.usecase.SignInUseCase
import javax.inject.Inject

class SignInUseCaseImpl @Inject constructor(
    private val authRepository: AuthRepository
) : SignInUseCase {
    override suspend fun invoke(email: String, password: String): Result<User> =
        authRepository.signInWithEmail(email, password)
}

