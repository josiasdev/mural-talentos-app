package com.edu.muraldetalentosapp.ui.model

import java.util.UUID
data class JobPosting(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val company: String,
    val type: String,
    val location: String,
    val salaryRange: String?,
    val publishedAt: String?,
    var isApplied: Boolean? = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val contractType: String?,
    val isSalaryNegotiable: Boolean?,
    val imageUrl: String? = null,
    val datePosted: Long?,
    val expirationDate: Long?
)