package com.edu.muraldetalentosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.muraldetalentosapp.data.model.Notification
import com.edu.muraldetalentosapp.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    val notifications: StateFlow<List<Notification>> = repository
        .getUserNotifications(auth.currentUser?.uid ?: "")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markAsRead(notification: Notification) {
        if (!notification.isRead) {
            viewModelScope.launch {
                repository.markAsRead(notification.id)
            }
        }
    }

    fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.markAllAsRead(userId)
        }
    }
}