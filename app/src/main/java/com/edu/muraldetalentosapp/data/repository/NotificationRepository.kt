package com.edu.muraldetalentosapp.data.repository

import android.util.Log
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
        try {
            val ref = notificationsCollection.add(notification).await()
            Log.d("NotificationRepo", "Created notification ${ref.id} for recipient=${notification.recipientId}")
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Failed to create notification for recipient=${notification.recipientId}", e)
            throw e
        }
    }

    fun getUserNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        Log.d("NotificationRepo", "Starting notifications listener for userId=$userId")

        // Tentativa inicial para diagnosticar problemas imediatos (permissões, índices, etc.)
        try {
            val initialSnapshot = notificationsCollection
                .whereEqualTo("recipientId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            Log.d("NotificationRepo", "Initial get returned ${initialSnapshot.size()} docs for user=$userId")
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Initial get failed for user=$userId: ${e.message}", e)
            // continua para abrir o listener mesmo após falha no get
        }

        val subscription = notificationsCollection
            .whereEqualTo("recipientId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepo", "getUserNotifications listener error for user=$userId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notification::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                Log.d("NotificationRepo", "Emitting ${notifications.size} notifications for user=$userId")
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