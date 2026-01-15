package com.edu.muraldetalentosapp.data.repository

import com.edu.muraldetalentosapp.data.model.Application
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ApplicationRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val applicationsCollection by lazy { firestore.collection("applications") }

    suspend fun applyToJob(application: Application) {
        val documentId = "${application.candidateId}_${application.jobId}"

        applicationsCollection.document(documentId).set(application).await()
    }

    suspend fun getUserApplications(candidateId: String): List<Application> {
        return try {
            val snapshot = applicationsCollection
                .whereEqualTo("candidateId", candidateId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Application::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getJobApplicationsCount(jobId: String): Int {
        return try {
            val snapshot = applicationsCollection
                .whereEqualTo("jobId", jobId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }
}
