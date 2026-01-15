package com.edu.muraldetalentosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.muraldetalentosapp.data.model.JobPosting
import com.edu.muraldetalentosapp.data.repository.JobPostingRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private val repository = JobPostingRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _jobs = MutableStateFlow<List<JobPosting>>(emptyList())
    val jobs: StateFlow<List<JobPosting>> = _jobs.asStateFlow()

    private val _uiState = MutableStateFlow(PostJobUiState())
    val uiState: StateFlow<PostJobUiState> = _uiState.asStateFlow()

    init {
        fetchJobs()
    }

    fun fetchJobs() {
        viewModelScope.launch {
            val jobList = repository.getAllActiveJobPostings()
            _jobs.value = jobList
        }
    }

    fun toggleApplication(jobId: String) {
        // No Firebase implementation for applications yet, keeping local toggle for UI feedback
        _jobs.update { currentList ->
            currentList.map { job ->
                if (job.id == jobId) {
                    job.copy(isApplied = !job.isApplied)
                } else {
                    job
                }
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

        val currentUser = auth.currentUser
        if (currentUser == null) return

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
                latitude = -4.9685, // Default for Quixadá
                longitude = -39.0150
            )

            try {
                repository.saveJobPosting(newJob)
                _uiState.update { PostJobUiState(isPostedSuccess = true) }
                fetchJobs() // Refresh the list
            } catch (e: Exception) {
                // Handle error
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
