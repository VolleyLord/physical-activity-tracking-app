package com.volleylord.gps_tracker.domain.usecase.impl

import com.volleylord.gps_tracker.domain.model.User
import com.volleylord.gps_tracker.domain.repository.AuthRepository
import com.volleylord.gps_tracker.domain.usecase.SignInWithGoogleUseCase
import javax.inject.Inject

class SignInWithGoogleUseCaseImpl @Inject constructor(
    private val authRepository: AuthRepository
) : SignInWithGoogleUseCase {
    override suspend fun invoke(idToken: String): Result<User> =
        authRepository.signInWithGoogle(idToken)
}

