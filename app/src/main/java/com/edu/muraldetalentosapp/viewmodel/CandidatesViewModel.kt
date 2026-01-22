package com.edu.muraldetalentosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.muraldetalentosapp.data.repository.CandidatesRepository
import com.edu.muraldetalentosapp.ui.model.CandidateUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CandidatesUiState {
    object Loading : CandidatesUiState()
    data class Success(
        val candidates: List<CandidateUiModel>,
        val totalCount: Int,
        val pendingCount: Int
    ) : CandidatesUiState()
    data class Error(val message: String) : CandidatesUiState()
}

class CandidatesViewModel(
    private val repository: CandidatesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CandidatesUiState>(CandidatesUiState.Loading)
    val uiState: StateFlow<CandidatesUiState> = _uiState.asStateFlow()
    private var currentJobId: String? = null

    fun loadCandidates(jobId: String) {
        currentJobId = jobId
        viewModelScope.launch {
            _uiState.value = CandidatesUiState.Loading
            try {
                val result = repository.getCandidatesForJob(jobId)

                _uiState.value = CandidatesUiState.Success(
                    candidates = result.candidates,
                    totalCount = result.totalCount,
                    pendingCount = result.pendingCount
                )
            } catch (e: Exception) {
                _uiState.value = CandidatesUiState.Error("Erro ao carregar candidatos: ${e.message}")
            }
        }
    }

    fun rejectCandidate(applicationId: String) {
        val jobId = currentJobId ?: return
        viewModelScope.launch {
            try {
                repository.rejectApplication(applicationId)
                // reload candidates for the same job
                loadCandidates(jobId)
            } catch (e: Exception) {
                _uiState.value = CandidatesUiState.Error("Erro ao rejeitar candidato: ${e.message}")
            }
        }
    }
}