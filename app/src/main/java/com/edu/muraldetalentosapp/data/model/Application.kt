package com.edu.muraldetalentosapp.data.model

import com.google.firebase.firestore.DocumentId

data class Application(
    @DocumentId val id: String = "",
    val jobId: String = "",
    val candidateId: String = "",
    val appliedAt: Long = System.currentTimeMillis()
)
