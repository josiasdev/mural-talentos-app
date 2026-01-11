package com.edu.muraldetalentosapp.ui.model

data class JobPosting(
    val title: String,
    val company: String,
    val type: String,
    val location: String,
    val salaryRange: String,
    val publishedAt: String,
    var isApplied: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)