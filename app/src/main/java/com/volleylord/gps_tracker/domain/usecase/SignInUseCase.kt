package com.volleylord.gps_tracker.domain.usecase

import com.volleylord.gps_tracker.domain.model.User

fun interface SignInUseCase {
    suspend operator fun invoke(email: String, password: String): Result<User>
}

