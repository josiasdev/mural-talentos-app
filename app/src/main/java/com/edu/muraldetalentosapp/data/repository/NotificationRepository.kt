package com.edu.muraldetalentosapp.data.repository

import com.edu.muraldetalentosapp.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val notificationsCollection = db.collection("notifications")

    suspend fun createNotification(notification: Notification) {
        notificationsCollection.add(notification).await()
    }

    fun getUserNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val subscription = notificationsCollection
            .whereEqualTo("recipientId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notification::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(notifications)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun markAsRead(notificationId: String) {
        notificationsCollection.document(notificationId)
            .update("isRead", true)
            .await()
    }

    suspend fun markAllAsRead(userId: String) {
        val snapshot = notificationsCollection
            .whereEqualTo("recipientId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .await()

        val batch = db.batch()
        snapshot.documents.forEach { doc ->
            batch.update(doc.reference, "isRead", true)
        }
        batch.commit().await()
    }
}