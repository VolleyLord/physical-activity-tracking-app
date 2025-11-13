package com.volleylord.gps_tracker.domain.fake

import com.volleylord.gps_tracker.domain.fake.repository.FakeActivitySessionRepository
import com.volleylord.gps_tracker.domain.model.ActivityStatus
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeActivitySessionRepositoryTest {

    @Test
    fun `startNewSession exposes active session`() = runTest {
        val repository = FakeActivitySessionRepository()

        val session = repository.startNewSession()

        val observed = repository.observeActiveSession().filterNotNull().first()
        assertEquals(session.id, observed.id)
        assertTrue(observed.status is ActivityStatus.Active)
    }

    @Test
    fun `stopSession moves session to history`() = runTest {
        val repository = FakeActivitySessionRepository()
        val session = repository.startNewSession()

        repository.stopSession(session.id)

        val history = repository.observeHistory(limit = 10).first()
        assertEquals(1, history.size)
        assertEquals(session.id, history.first().id)
        assertTrue(history.first().status is ActivityStatus.Completed)
    }
}

