package com.edu.muraldetalentosapp.data.repository

import com.edu.muraldetalentosapp.data.model.Application
import com.edu.muraldetalentosapp.data.model.User
import com.edu.muraldetalentosapp.ui.model.CandidateUiModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.edu.muraldetalentosapp.data.model.ApplicationStatus
import com.edu.muraldetalentosapp.data.model.Notification

// Resultado com lista de candidatos e metadados
data class CandidatesResult(
    val candidates: List<CandidateUiModel>,
    val totalCount: Int,
    val pendingCount: Int
)

class CandidatesRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val notificationRepository = NotificationRepository()

    suspend fun getCandidatesForJob(jobId: String): CandidatesResult {
        val applicationsSnapshot = db.collection("applications")
            .whereEqualTo("jobId", jobId)
            .get()
            .await()

        // Mapeia cada documento para Application e preserva o document id
        val applications = applicationsSnapshot.documents.mapNotNull { doc ->
            try {
                doc.toObject(Application::class.java)?.copy(id = doc.id)
            } catch (e: Exception) {
                null
            }
        }

        val candidatesList = mutableListOf<CandidateUiModel>()

        for (app in applications) {
            // Segurança: requisições externas somente se candidateId estiver presente
            val candidateId = app.candidateId
            if (candidateId.isBlank()) continue

            try {
                val userSnapshot = db.collection("users")
                    .document(candidateId)
                    .get()
                    .await()

                val user = userSnapshot.toObject(User::class.java)

                if (user != null) {
                    // Tenta ler resumeUrl do documento (salvo no profile do candidato)
                    val resumeUrl = userSnapshot.getString("resumeUrl")

                    candidatesList.add(
                        CandidateUiModel(
                            applicationId = app.id,
                            candidateId = user.uid,
                            name = user.name,
                            email = user.email,
                            phone = user.phone,
                            appliedAt = app.appliedAt,
                            status = app.status,
                            resumeUrl = resumeUrl
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Ordena por data de candidatura (mais recente primeiro)
        val sorted = candidatesList.sortedByDescending { it.appliedAt }

        val total = sorted.size
        val pending = sorted.count { it.status == ApplicationStatus.PENDING }

        return CandidatesResult(
            candidates = sorted,
            totalCount = total,
            pendingCount = pending
        )
    }

    suspend fun updateStatus(
        applicationId: String,
        newStatus: String,
        candidateId: String,
        jobTitle: String
    ) {
        db.collection("applications")
            .document(applicationId)
            .update("status", newStatus)
            .await()

        val notification = Notification(
            recipientId = candidateId,
            title = "Status Atualizado: $jobTitle",
            message = "Sua candidatura mudou para: $newStatus. Verifique seu progresso.",
            jobId = applicationId
        )

        notificationRepository.createNotification(notification)
    }
}