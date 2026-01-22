package com.edu.muraldetalentosapp.data.model

import com.google.firebase.firestore.DocumentId

enum class ApplicationStatus {
    PENDING, ANALYZED, CONTACTED, REJECTED
}

data class Application(
    @DocumentId val id: String = "",
    val jobId: String = "",
    val candidateId: String = "",
    val appliedAt: Long = System.currentTimeMillis(),
    val status: ApplicationStatus = ApplicationStatus.PENDING
)
