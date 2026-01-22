package com.edu.muraldetalentosapp.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.muraldetalentosapp.data.model.CandidateProfile
import com.edu.muraldetalentosapp.data.repository.UserRepository
import com.edu.muraldetalentosapp.ui.screen.ProfileUiState
import com.edu.muraldetalentosapp.ui.screen.UploadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import com.edu.muraldetalentosapp.BuildConfig

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    // Carrega os dados assim que a tela abre
    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val profile = userRepository.getCandidateProfile()
            if (profile != null) {
                uiState = uiState.copy(
                    fullName = profile.name,
                    email = profile.email,
                    cpf = profile.cpf,
                    fileName = profile.resumeFileName // Mostra o nome do arquivo já salvo
                )
            } else {
                // Se não tiver perfil salvo, tenta pegar nome/email do Auth
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    uiState = uiState.copy(
                        fullName = user.displayName ?: "",
                        email = user.email ?: ""
                    )
                }
            }
        }
    }

    fun onNameChange(v: String) { uiState = uiState.copy(fullName = v) }
    fun onEmailChange(v: String) { uiState = uiState.copy(email = v); validateEmail() }
    fun onCpfChange(v: String) { uiState = uiState.copy(cpf = v); validateCpf() }

    fun onFileSelected(uri: Uri, fileName: String) {
        uiState = uiState.copy(selectedFileUri = uri, fileName = fileName, uploadState = UploadState.Idle)
    }

    private fun validateEmail() {
        val valid = Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches()
        uiState = uiState.copy(emailError = if (!valid) "E-mail inválido" else null)
    }

    private fun validateCpf() {
        // Validação simples de tamanho
        val valid = uiState.cpf.length >= 11
        uiState = uiState.copy(cpfError = if (!valid) "CPF incompleto" else null)
    }

    fun saveData(context: Context, onSuccess: () -> Unit) {

        Log.d("DEBUG_SUPABASE", "URL: ${BuildConfig.SUPABASE_URL}")
        Log.d("DEBUG_SUPABASE", "KEY: ${BuildConfig.SUPABASE_ANON_KEY}")

        validateEmail()
        validateCpf()
        if (uiState.emailError != null || uiState.cpfError != null || uiState.fullName.isBlank()) return

        val user = userRepository.getCurrentUser() ?: return

        uiState = uiState.copy(uploadState = UploadState.Uploading)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var currentResumeUrl: String? = null
                var currentFileName = uiState.fileName

                // 1. UPLOAD NO SUPABASE (se houver arquivo novo selecionado)
                if (uiState.selectedFileUri != null) {
                    val bytes = context.contentResolver.openInputStream(uiState.selectedFileUri!!)?.use {
                        it.readBytes()
                    } ?: throw Exception("Erro ao ler arquivo")

                    currentResumeUrl = userRepository.uploadResumeToSupabase(
                        userId = user.uid,
                        fileName = uiState.fileName ?: "curriculo.pdf",
                        fileBytes = bytes
                    )
                } else {
                    val oldProfile = userRepository.getCandidateProfile()
                    currentResumeUrl = oldProfile?.resumeUrl
                    currentFileName = oldProfile?.resumeFileName
                }

                val profile = CandidateProfile(
                    uid = user.uid,
                    name = uiState.fullName,
                    email = uiState.email,
                    cpf = uiState.cpf,
                    resumeUrl = currentResumeUrl,
                    resumeFileName = currentFileName
                )
                userRepository.saveCandidateProfile(profile)

                // Novo: se salvou profile com sucesso, marca o usuário como completo
                userRepository.markUserComplete(user.uid)

                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(uploadState = UploadState.Success)
                    Toast.makeText(context, "Perfil salvo!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(uploadState = UploadState.Error)
                    Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}