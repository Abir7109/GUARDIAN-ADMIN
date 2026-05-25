package com.securphone.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.securphone.app.data.models.EventModel
import com.securphone.app.utils.Constants
import kotlinx.coroutines.tasks.await

object EventRepository {
    private val localEvents = java.util.Collections.synchronizedList(mutableListOf<EventModel>())
    private val firestore = FirebaseFirestore.getInstance()

    fun getLocalEvents(): List<EventModel> {
        synchronized(localEvents) {
            return localEvents.toList()
        }
    }

    suspend fun logEvent(event: EventModel) {
        synchronized(localEvents) {
            localEvents.add(event)
        }
        try {
            val docRef = if (event.id.isEmpty()) {
                firestore.collection(Constants.COLLECTION_EVENTS).document()
            } else {
                firestore.collection(Constants.COLLECTION_EVENTS).document(event.id)
            }
            docRef.set(event).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getRecentEvents(userId: String, limit: Long = 10): List<EventModel> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_EVENTS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(EventModel::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
