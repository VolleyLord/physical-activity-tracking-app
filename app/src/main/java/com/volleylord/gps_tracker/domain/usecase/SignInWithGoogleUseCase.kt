package com.volleylord.gps_tracker.domain.usecase

import com.volleylord.gps_tracker.domain.model.User

fun interface SignInWithGoogleUseCase {
    suspend operator fun invoke(idToken: String): Result<User>
}

