package com.edu.muraldetalentosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.muraldetalentosapp.data.model.User
import com.edu.muraldetalentosapp.data.repository.UserRepository
import com.edu.muraldetalentosapp.ui.components.AccountType
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userType = MutableStateFlow(AccountType.CANDIDATE)
    val userType: StateFlow<AccountType> = _userType.asStateFlow()

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email e senha não podem estar em branco.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val firebaseUser = userRepository.signIn(email, password)
                if (firebaseUser != null) {
                    val profile = userRepository.getUserProfile(firebaseUser.uid)
                    profile?.let {
                        _userType.value = it.type
                    }
                    _authState.value = AuthState.Success(firebaseUser)
                } else {
                    _authState.value = AuthState.Error("Login falhou: usuário nulo")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login falhou")
            }
        }
    }

    fun signUp(email: String, password: String, name: String, type: AccountType, phone: String = "", about: String = "") {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _authState.value = AuthState.Error("Nome, email e senha não podem estar em branco.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val userModel = User(
                    name = name,
                    email = email,
                    type = type,
                    phone = phone,
                    about = about
                )
                val firebaseUser = userRepository.signUp(userModel, password)
                if (firebaseUser != null) {
                    _userType.value = type
                    _authState.value = AuthState.Success(firebaseUser)
                } else {
                    _authState.value = AuthState.Error("Cadastro falhou: usuário nulo")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Cadastro falhou")
            }
        }
    }

    fun signOut() {
        userRepository.signOut()
        _authState.value = AuthState.Idle
        _userType.value = AccountType.CANDIDATE
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
