package com.edu.muraldetalentosapp.data.model

import com.google.firebase.firestore.DocumentId

data class JobPosting(
    @DocumentId val id: String = "",
    val title: String = "",
    val company: String = "",
    val companyId: String = "",
    val description: String = "",
    val location: String = "",
    val type: String = "", // Ex: CLT, PJ
    val contractType: String = "",
    val salaryRange: String = "",
    val isSalaryNegotiable: Boolean = false,
    val publishedAt: String = "",
    val datePosted: Long = 0,
    val expirationDate: Long = 0,
    val isApplied: Boolean = false,
    val imageUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isActive: Boolean = true
)
