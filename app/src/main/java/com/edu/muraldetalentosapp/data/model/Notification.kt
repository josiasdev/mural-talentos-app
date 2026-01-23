package com.edu.muraldetalentosapp.data.model

data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val title: String = "",
    val message: String = "",
    val jobId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)