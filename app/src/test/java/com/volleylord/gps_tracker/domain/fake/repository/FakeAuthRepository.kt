package com.volleylord.gps_tracker.domain.fake.repository

import com.volleylord.gps_tracker.domain.model.User
import com.volleylord.gps_tracker.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository(
    defaultUser: User? = null
) : AuthRepository {

    private val userState = MutableStateFlow(defaultUser)

    override val currentUser: Flow<User?> = userState.asStateFlow()

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        val signedInUser = User(
            uid = "fake-${email.hashCode()}",
            email = email,
            displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        )
        userState.value = signedInUser
        return Result.success(signedInUser)
    }

    override suspend fun signOut() {
        userState.value = null
    }
}