package com.edu.muraldetalentosapp.data.repository

import com.edu.muraldetalentosapp.data.model.Application
import com.edu.muraldetalentosapp.data.model.User
import com.edu.muraldetalentosapp.ui.model.CandidateUiModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.edu.muraldetalentosapp.data.model.ApplicationStatus

// Resultado com lista de candidatos e metadados
data class CandidatesResult(
    val candidates: List<CandidateUiModel>,
    val totalCount: Int,
    val pendingCount: Int
)

class CandidatesRepository(private val db: FirebaseFirestore) {

    suspend fun getCandidatesForJob(jobId: String): CandidatesResult {
        val applicationsSnapshot = db.collection("applications")
            .whereEqualTo("jobId", jobId)
            .get()
            .await()

        val applications = applicationsSnapshot.toObjects(Application::class.java)
        val candidatesList = mutableListOf<CandidateUiModel>()


        for (app in applications) {
            try {
                val userSnapshot = db.collection("users")
                    .document(app.candidateId)
                    .get()
                    .await()

                val user = userSnapshot.toObject(User::class.java)

                if (user != null) {
                    candidatesList.add(
                        CandidateUiModel(
                            applicationId = app.id,
                            candidateId = user.uid,
                            name = user.name,
                            email = user.email,
                            phone = user.phone,
                            appliedAt = app.appliedAt,
                            status = app.status
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val total = candidatesList.size
        val pending = candidatesList.count { it.status == ApplicationStatus.PENDING }

        return CandidatesResult(
            candidates = candidatesList,
            totalCount = total,
            pendingCount = pending
        )
    }

    suspend fun updateStatus(applicationId: String, newStatus: String) {
        db.collection("applications")
            .document(applicationId)
            .update("status", newStatus)
            .await()
    }
}