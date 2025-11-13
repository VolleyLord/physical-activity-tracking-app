package com.volleylord.gps_tracker.domain.fake

import com.volleylord.gps_tracker.domain.fake.repository.FakeAuthRepository
import com.volleylord.gps_tracker.domain.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FakeAuthRepositoryTest {

    @Test
    fun `signInWithEmail updates current user`() = runTest {
        val repository = FakeAuthRepository()

        repository.signInWithEmail("test@example.com", "password")

        val user = repository.currentUser.first()
        assertEquals("test@example.com", user?.email)
    }

    @Test
    fun `signOut clears current user`() = runTest {
        val repository = FakeAuthRepository(defaultUser = User("id", "mail", "name"))

        repository.signOut()

        assertNull(repository.currentUser.first())
    }
}

