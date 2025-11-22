package com.volleylord.gps_tracker.data.remote.api

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.volleylord.gps_tracker.data.remote.dto.ActivitySessionDto
import com.volleylord.gps_tracker.data.remote.mapper.ActivitySessionMapper
import com.volleylord.gps_tracker.domain.model.ActivitySession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source for activity sessions.
 * Handles CRUD operations for sessions in Firestore.
 */
@Singleton
class FirestoreActivitySessionDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val sessionsCollection = firestore.collection("activity_sessions")

    /**
     * Observes the active session for a user.
     * Returns null if no active session exists.
     */
    fun observeActiveSession(userId: String): Flow<ActivitySession?> = callbackFlow {
        val listener = sessionsCollection
            .whereEqualTo("user_id", userId)
            .whereEqualTo("status", "active")
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val session = snapshot?.documents?.firstOrNull()?.let { doc ->
                    val dto = doc.toObject(ActivitySessionDto::class.java)
                    dto?.let { ActivitySessionMapper.dtoToDomain(doc.id, it) }
                }
                trySend(session)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Creates a new session document in Firestore.
     */
    suspend fun createSession(session: ActivitySession): String {
        val dto = ActivitySessionMapper.domainToDto(session)
        val docRef = sessionsCollection.add(dto).await()
        return docRef.id
    }

    /**
     * Updates an existing session document.
     */
    suspend fun updateSession(sessionId: String, session: ActivitySession) {
        val dto = ActivitySessionMapper.domainToDto(session)
        sessionsCollection.document(sessionId).set(dto).await()
    }

    /**
     * Observes session history for a user, ordered by start time descending.
     */
    fun observeHistory(userId: String, limit: Int): Flow<List<ActivitySession>> = callbackFlow {
        val listener = sessionsCollection
            .whereEqualTo("user_id", userId)
            .whereEqualTo("status", "completed")
            .orderBy("started_at_epoch_millis", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val sessions = snapshot?.documents?.mapNotNull { doc ->
                    val dto = doc.toObject(ActivitySessionDto::class.java)
                    dto?.let { ActivitySessionMapper.dtoToDomain(doc.id, it) }
                } ?: emptyList()
                trySend(sessions)
            }

        awaitClose { listener.remove() }
    }

    fun observeSessionById(sessionId: String): Flow<ActivitySession?> = callbackFlow {
        val listener = sessionsCollection.document(sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val dto = snapshot?.toObject(ActivitySessionDto::class.java)
                val session = dto?.let { ActivitySessionMapper.dtoToDomain(snapshot.id, it) }
                trySend(session)
            }

        awaitClose { listener.remove() }
    }

    suspend fun updateSessionNotes(sessionId: String, notes: String) {
        sessionsCollection.document(sessionId)
            .update("notes", notes)
            .await()
    }
}

