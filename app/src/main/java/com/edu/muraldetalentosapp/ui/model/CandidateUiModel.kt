package com.edu.muraldetalentosapp.ui.model

import com.edu.muraldetalentosapp.data.model.ApplicationStatus

data class CandidateUiModel(
    val applicationId: String,
    val candidateId: String,
    val name: String,
    val email: String,
    val phone: String,
    val appliedAt: Long,
    val status: ApplicationStatus,
    val resumeUrl: String? = null
)