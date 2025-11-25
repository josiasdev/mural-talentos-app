package com.edu.muraldetalentosapp.ui.model

data class JobPosting(
    val title: String,
    val company: String,
    val type: String,
    val location: String,
    val salaryRange: String,
    val publishedAt: String,
    var isApplied: Boolean = false
)