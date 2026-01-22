package com.edu.muraldetalentosapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.muraldetalentosapp.data.model.Application
import com.edu.muraldetalentosapp.data.model.JobPosting
import com.edu.muraldetalentosapp.data.repository.ApplicationRepository
import com.edu.muraldetalentosapp.data.repository.JobPostingRepository
import com.edu.muraldetalentosapp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow

data class PostJobUiState(
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val contractType: String = "",
    val salary: String = "",
    val isSalaryNegotiable: Boolean = false,
    val imageUrl: String = "",
    val titleError: Boolean = false,
    val descriptionError: Boolean = false,
    val locationError: Boolean = false,
    val contractError: Boolean = false,
    val salaryError: Boolean = false,
    val isPostedSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)

class JobsViewModel : ViewModel() {
    private val jobRepository = JobPostingRepository()
    private val applicationRepository = ApplicationRepository()
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    // Raw jobs provided by repository
    private val _rawJobs = MutableStateFlow<List<JobPosting>>(emptyList())

    // Applied ids from repository (realtime)
    private val _dbAppliedIds = MutableStateFlow<Set<String>>(emptySet())

    // Local optimistic cache
    private val _optimisticAppliedIds = MutableStateFlow<Set<String>>(emptySet())

    val jobs: StateFlow<List<JobPosting>> = combine(
        _rawJobs, _dbAppliedIds, _optimisticAppliedIds
    ) { rawJobs, dbIds, optimisticIds ->
        val allAppliedIds = dbIds + optimisticIds
        rawJobs.map { job ->
            job.copy(isApplied = allAppliedIds.contains(job.id))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(PostJobUiState())
    val uiState: StateFlow<PostJobUiState> = _uiState.asStateFlow()

    private val _jobApplicationCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val jobApplicationCounts: StateFlow<Map<String, Int>> = _jobApplicationCounts.asStateFlow()

    private val _userApplicationStatuses = MutableStateFlow<Map<String, String>>(emptyMap())
    val userApplicationStatuses: StateFlow<Map<String, String>> = _userApplicationStatuses.asStateFlow()

    private val _applyAlert = MutableStateFlow<String?>(null)
    val applyAlert: StateFlow<String?> = _applyAlert.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    // Jobs to manage collectors so we can cancel/restart them when auth changes
    private var jobsCollectorJob: Job? = null
    private var appliedIdsCollectorJob: Job? = null
    private var countsCollectorJob: Job? = null
    private var authListener: FirebaseAuth.AuthStateListener? = null

    init {
        // Registra listener de autenticação e inicia listeners conforme o usuário
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            startListeners(uid)
        }
        authListener?.let { auth.addAuthStateListener(it) }

        // Inicia imediatamente com o usuário atual (se estiver logado)
        startListeners(auth.currentUser?.uid)
    }

    private fun startListeners(uid: String?) {
        // Cancela coletores anteriores
        jobsCollectorJob?.cancel()
        appliedIdsCollectorJob?.cancel()
        countsCollectorJob?.cancel()

        // Sempre iniciar listener de vagas ativas (disponível para todos)
        jobsCollectorJob = viewModelScope.launch {
            Log.d("JobsViewModel", "Starting jobs listener (uid=$uid)")
            jobRepository.listenToActiveJobs().collect { list ->
                Log.d("JobsViewModel", "Received ${'$'}{list.size} jobs from repository")
                _rawJobs.value = list
            }
        }

        // One-shot: tenta buscar as vagas que a empresa publicou (útil se listener não retornar por regras ou demora)
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val myJobs = jobRepository.getJobPostingsByCompany(uid)
                    if (myJobs.isNotEmpty()) {
                        Log.d("JobsViewModel", "Fetched ${'$'}{myJobs.size} company jobs via direct query for uid=$uid")
                        // Mescla com jobs atuais, preservando unicidade
                        val current = _rawJobs.value
                        val merged = (current + myJobs).distinctBy { it.id }
                        _rawJobs.value = merged

                        // Inicia listener de contagens apenas para as vagas desta empresa
                        countsCollectorJob?.cancel()
                        countsCollectorJob = viewModelScope.launch {
                            applicationRepository.listenToApplicationCountsForJobIds(myJobs.map { it.id }).collect { counts ->
                                Log.d("JobsViewModel", "Received ${'$'}{counts.size} job counts for company")
                                _jobApplicationCounts.value = counts
                            }
                        }
                    } else {
                        Log.d("JobsViewModel", "No company jobs found via direct query for uid=$uid")
                    }
                } catch (e: Exception) {
                    Log.e("JobsViewModel", "Error fetching company jobs directly", e)
                }
            }
        }

        // Se não ha usuario autenticado, zera apenas os estados dependentes de uid e não inicia esses listeners
        if (uid == null) {
            _dbAppliedIds.value = emptySet()
            _jobApplicationCounts.value = emptyMap()
            _userApplicationStatuses.value = emptyMap()
            return
        }

        // Listener de candidaturas do usuário
        appliedIdsCollectorJob = viewModelScope.launch {
            Log.d("JobsViewModel", "Starting appliedIds listener for uid=$uid")
            applicationRepository.listenToUserAppliedJobIds(uid).collect { ids ->
                Log.d("JobsViewModel", "Received ${'$'}{ids.size} applied ids")
                _dbAppliedIds.value = ids
            }
        }

        // Listener de status das aplicações do usuário
        viewModelScope.launch {
            applicationRepository.listenToUserApplicationStatuses(uid).collect { map ->
                _userApplicationStatuses.value = map
            }
        }
    }

    fun applyToJob(jobId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            // Delega checagem e operação ao repositório
            val application = Application(
                jobId = jobId,
                candidateId = currentUser.uid
            )

            val applied = try {
                applicationRepository.tryApplyToJob(application, userRepository)
            } catch (e: Exception) {
                Log.e("JobsViewModel", "Erro ao aplicar à vaga via repositório", e)
                false
            }

            if (!applied) {
                setApplyAlert("Complete seu cadastro para se candidatar à vaga.")
                Log.d("JobsViewModel", "Usuário não completo - aplicação bloqueada para vaga $jobId")
                return@launch
            }

            // Se chegou aqui, repositório aplicou com sucesso
            _optimisticAppliedIds.update { it + jobId }
            Log.d("JobsViewModel", "Aplicação enviada com sucesso para $jobId")
        }
    }

    fun fetchJobs() {
        // Método de conveniência: os listeners já estão conectados no init
    }

    // Public method to force fetching company jobs (one-shot) and merge into current jobs
    fun fetchCompanyJobs() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val myJobs = jobRepository.getJobPostingsByCompany(uid)
                Log.d("JobsViewModel", "fetchCompanyJobs: got ${'$'}{myJobs.size} jobs for uid=$uid")
                if (myJobs.isNotEmpty()) {
                    val current = _rawJobs.value
                    val merged = (current + myJobs).distinctBy { it.id }
                    _rawJobs.value = merged

                    // restart counts listener for these job ids
                    countsCollectorJob?.cancel()
                    countsCollectorJob = viewModelScope.launch {
                        applicationRepository.listenToApplicationCountsForJobIds(myJobs.map { it.id }).collect { counts ->
                            Log.d("JobsViewModel", "Received ${'$'}{counts.size} job counts for company")
                            _jobApplicationCounts.value = counts
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("JobsViewModel", "fetchCompanyJobs failed", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cancela collectors e remove auth listener
        jobsCollectorJob?.cancel()
        appliedIdsCollectorJob?.cancel()
        countsCollectorJob?.cancel()
        authListener?.let { auth.removeAuthStateListener(it) }
    }

    fun toggleApplication(jobId: String) {
        applyToJob(jobId)
    }

    fun closeJob(jobId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                jobRepository.closeJob(jobId)
                Log.d("JobsViewModel", "Vaga $jobId fechada com sucesso")
            } catch (e: Exception) {
                Log.e("JobsViewModel", "Erro ao fechar vaga $jobId", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTitleChange(newValue: String) { _uiState.update { it.copy(title = newValue, titleError = false) } }
    fun onDescriptionChange(newValue: String) { _uiState.update { it.copy(description = newValue, descriptionError = false) } }
    fun onLocationChange(newValue: String) { _uiState.update { it.copy(location = newValue, locationError = false) } }
    fun onContractTypeChange(newValue: String) { _uiState.update { it.copy(contractType = newValue, contractError = false) } }
    fun onSalaryChange(newValue: String) { _uiState.update { it.copy(salary = newValue, salaryError = false) } }
    fun onSalaryNegotiableChange(newValue: Boolean) {
        _uiState.update { it.copy(isSalaryNegotiable = newValue, salaryError = false) }
    }
    fun onImageUrlChange(newValue: String) { _uiState.update { it.copy(imageUrl = newValue) } }
    fun onLatLongChange(lat: Double, long: Double) { _uiState.update { it.copy(latitude = lat, longitude = long) } }

    fun resetSuccessMessage() {
        _uiState.update { it.copy(isPostedSuccess = false) }
    }

    fun setApplyAlert(message: String?) {
        _applyAlert.value = message
    }
    fun clearApplyAlert() {
        _applyAlert.value = null
    }

    fun publishJob() {
        val state = _uiState.value
        var hasError = false

        if (state.title.isBlank()) { _uiState.update { it.copy(titleError = true) }; hasError = true }
        if (state.description.isBlank()) { _uiState.update { it.copy(descriptionError = true) }; hasError = true }
        if (state.location.isBlank()) { _uiState.update { it.copy(locationError = true) }; hasError = true }
        if (state.contractType.isBlank()) { _uiState.update { it.copy(contractError = true) }; hasError = true }
        if (!state.isSalaryNegotiable && state.salary.isBlank()) { _uiState.update { it.copy(salaryError = true) }; hasError = true }

        if (hasError) return

        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Delegamos a construção do JobPosting e persistência ao repository
                jobRepository.publishJob(
                    title = state.title,
                    description = state.description,
                    location = state.location,
                    contractType = state.contractType,
                    salary = state.salary,
                    isSalaryNegotiable = state.isSalaryNegotiable,
                    imageUrl = state.imageUrl.ifBlank { null },
                    latitude = state.latitude,
                    longitude = state.longitude,
                    companyId = currentUser.uid,
                    companyName = currentUser.displayName ?: "Empresa"
                )

                _uiState.update { it.copy(isPostedSuccess = true, isLoading = false) }
            } catch (e: Exception) {
                Log.e("JobsViewModel", "Erro ao publicar vaga", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun uploadImage(context: android.content.Context, uri: android.net.Uri) {
        _isUploadingImage.value = true
        viewModelScope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val imageUrl = jobRepository.uploadJobImage(bytes)
                    onImageUrlChange(imageUrl)
                }
            } catch (e: Exception) {
                Log.e("JobsViewModel", "Erro ao enviar imagem da vaga", e)
            } finally {
                _isUploadingImage.value = false
            }
        }
    }
}
