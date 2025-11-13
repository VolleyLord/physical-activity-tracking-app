package com.volleylord.gps_tracker.domain.repository

import com.volleylord.gps_tracker.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signOut()
}

