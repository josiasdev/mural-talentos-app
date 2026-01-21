package com.edu.muraldetalentosapp.data.repository

import com.edu.muraldetalentosapp.data.model.User
import com.google.firebase.auth.FirebaseUser

sealed class AuthResult {
    data class Success(val firebaseUser: FirebaseUser, val profile: User?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val userRepository: UserRepository = UserRepository()
) {

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val firebaseUser = userRepository.signIn(email, password)
            if (firebaseUser == null) return AuthResult.Error("Login falhou: usuário nulo")

            val profile = userRepository.getUserProfile(firebaseUser.uid)
            AuthResult.Success(firebaseUser, profile)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login falhou")
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        type: com.edu.muraldetalentosapp.ui.components.AccountType,
        phone: String = "",
        about: String = ""
    ): AuthResult {
        return try {
            val userModel = User(
                name = name,
                email = email,
                type = type,
                phone = phone,
                about = about
            )
            val firebaseUser = userRepository.signUp(userModel, password)
            if (firebaseUser == null) return AuthResult.Error("Cadastro falhou: usuário nulo")

            val savedProfile = userRepository.getUserProfile(firebaseUser.uid)
            AuthResult.Success(firebaseUser, savedProfile)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Cadastro falhou")
        }
    }

    fun signOut() {
        userRepository.signOut()
    }
}
