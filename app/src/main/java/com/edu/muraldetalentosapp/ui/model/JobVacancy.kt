package com.edu.muraldetalentosapp.ui.model

data class JobVacancy (
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val contractType: String = "",
    val isSalaryNegotiable: Boolean = false,
    val salaryRange: String = "",
    val imageUrl: String? = null,
    val userId: String = ""
)