package com.edu.muraldetalentosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.muraldetalentosapp.data.repository.AuthRepository
import com.edu.muraldetalentosapp.data.repository.AuthResult
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

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

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
                when (val result = authRepository.signIn(email, password)) {
                    is AuthResult.Success -> {
                        val firebaseUser = result.firebaseUser
                        result.profile?.let { _userType.value = it.type }
                        _authState.value = AuthState.Success(firebaseUser)
                    }
                    is AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
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
                when (val result = authRepository.signUp(email, password, name, type, phone, about)) {
                    is AuthResult.Success -> {
                        val firebaseUser = result.firebaseUser
                        result.profile?.let { _userType.value = it.type }
                        _authState.value = AuthState.Success(firebaseUser)
                    }
                    is AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Cadastro falhou")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Idle
        _userType.value = AccountType.CANDIDATE
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
