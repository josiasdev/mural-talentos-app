package com.edu.muraldetalentosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.muraldetalentosapp.data.model.Notification
import com.edu.muraldetalentosapp.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // Mantém o uid corrente e troca a fonte de notificações quando mudar
    private val _uid = MutableStateFlow(auth.currentUser?.uid ?: "")

    init {
        // atualiza se o auth mudar (login/logout)
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            _uid.value = firebaseAuth.currentUser?.uid ?: ""
        }
        auth.addAuthStateListener(listener)
    }

    val notifications: StateFlow<List<Notification>> = _uid
        .flatMapLatest { uid ->
            if (uid.isBlank()) flowOf(emptyList()) else repository.getUserNotifications(uid)
        }
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