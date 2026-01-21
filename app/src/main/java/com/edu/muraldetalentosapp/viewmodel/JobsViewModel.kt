package com.edu.muraldetalentosapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.muraldetalentosapp.data.model.Application
import com.edu.muraldetalentosapp.data.model.JobPosting
import com.edu.muraldetalentosapp.data.repository.ApplicationRepository
import com.edu.muraldetalentosapp.data.repository.JobPostingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

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
    val isLoading: Boolean = false
)

class JobsViewModel : ViewModel() {
    private val jobRepository = JobPostingRepository()
    private val applicationRepository = ApplicationRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _rawJobs = MutableStateFlow<List<JobPosting>>(emptyList())
    
    private val _dbAppliedIds = MutableStateFlow<Set<String>>(emptySet())
    
    private val _optimisticAppliedIds = MutableStateFlow<Set<String>>(emptySet())

    // Nova flow para comunicar alertas ao UI quando o usuário não completou o cadastro
    private val _applyAlert = MutableStateFlow<String?>(null)
    val applyAlert: StateFlow<String?> = _applyAlert.asStateFlow()

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

    private var jobsListener: ListenerRegistration? = null
    private var appsListener: ListenerRegistration? = null
    private var countsListener: ListenerRegistration? = null
    private var authListener: FirebaseAuth.AuthStateListener? = null

    init {
        authListener = FirebaseAuth.AuthStateListener { 
            setupRealtimeListeners() 
        }
        auth.addAuthStateListener(authListener!!)
    }

    private fun setupRealtimeListeners() {
        val currentUser = auth.currentUser
        
        jobsListener?.remove()
        appsListener?.remove()
        countsListener?.remove()

        jobsListener = db.collection("jobs")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, _ ->
                _rawJobs.value = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(JobPosting::class.java)?.copy(id = doc.id)
                } ?: emptyList()
            }

        if (currentUser != null) {
            appsListener = db.collection("applications")
                .whereEqualTo("candidateId", currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    val ids = snapshot?.documents?.mapNotNull { it.getString("jobId") }?.toSet() ?: emptySet()
                    _dbAppliedIds.value = ids
                }
        } else {
            _dbAppliedIds.value = emptySet()
            _optimisticAppliedIds.value = emptySet()
        }

        countsListener = db.collection("applications")
            .addSnapshotListener { snapshot, _ ->
                val counts = snapshot?.documents?.groupBy { it.getString("jobId") ?: "" }
                    ?.mapValues { it.value.size } ?: emptyMap()
                _jobApplicationCounts.value = counts
            }
    }

    fun applyToJob(jobId: String) {
        val currentUser = auth.currentUser ?: return

        // Checagem do campo `isComplete` no Firestore antes de aplicar.
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                val isComplete = userDoc.getBoolean("isComplete") ?: false

                if (!isComplete) {
                    // Expor mensagem de alerta para a UI mostrar um aviso vermelho
                    _applyAlert.value = "Complete seu cadastro e adicione o currículo antes de se candidatar a vagas."
                    Log.d("JobsViewModel", "Usuário não completou cadastro: abortando candidatura para $jobId")
                    return@launch
                }

                // Agora que está completo, procedemos com a inscrição (cache otimista e persistência)
                _optimisticAppliedIds.update { it + jobId }
                Log.d("JobsViewModel", "Adicionado ao cache otimista: $jobId")

                try {
                    val application = Application(
                        jobId = jobId,
                        candidateId = currentUser.uid
                    )
                    applicationRepository.applyToJob(application)
                    Log.d("JobsViewModel", "Sucesso no Firestore para $jobId")
                } catch (e: Exception) {
                    Log.e("JobsViewModel", "Erro ao salvar, removendo do cache otimista", e)
                    _optimisticAppliedIds.update { it - jobId }
                }

            } catch (e: Exception) {
                Log.e("JobsViewModel", "Erro ao verificar isComplete do usuário", e)
                _applyAlert.value = "Não foi possível verificar o status do perfil. Tente novamente mais tarde."
            }
        }
    }

    // Método para permitir que a UI limpe o alerta após exibir
    fun clearApplyAlert() {
        _applyAlert.value = null
    }

    fun fetchJobs() {
        setupRealtimeListeners()
    }

    override fun onCleared() {
        super.onCleared()
        authListener?.let { auth.removeAuthStateListener(it) }
        jobsListener?.remove()
        appsListener?.remove()
        countsListener?.remove()
    }

    fun toggleApplication(jobId: String) {
        applyToJob(jobId)
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

    fun resetSuccessMessage() {
        _uiState.update { it.copy(isPostedSuccess = false) }
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
            
            val currentTimestamp = System.currentTimeMillis()
            val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
            val expirationTimestamp = currentTimestamp + thirtyDaysInMillis
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(currentTimestamp))

            val newJob = JobPosting(
                title = state.title,
                company = currentUser.displayName ?: "Empresa",
                companyId = currentUser.uid,
                description = state.description,
                location = state.location,
                type = state.contractType,
                contractType = state.contractType,
                salaryRange = if (state.isSalaryNegotiable) "A combinar" else state.salary,
                isSalaryNegotiable = state.isSalaryNegotiable,
                publishedAt = formattedDate,
                datePosted = currentTimestamp,
                expirationDate = expirationTimestamp,
                isApplied = false,
                imageUrl = state.imageUrl.ifBlank { null },
                latitude = -4.9685, 
                longitude = -39.0150
            )

            try {
                jobRepository.saveJobPosting(newJob)
                _uiState.update { PostJobUiState(isPostedSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
