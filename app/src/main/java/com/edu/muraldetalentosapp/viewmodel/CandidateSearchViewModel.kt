package com.edu.muraldetalentosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.muraldetalentosapp.data.model.User
import com.edu.muraldetalentosapp.data.repository.ApplicationRepository
import com.edu.muraldetalentosapp.data.repository.JobPostingRepository
import com.edu.muraldetalentosapp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CandidateUiItem(
    val user: User,
    val applicationsCount: Int,
    val hasResume: Boolean = false,
    val registrationDateFormatted: String,
    val initials: String
)

class CandidateSearchViewModel(
    private val jobRepository: JobPostingRepository,
    private val applicationRepository: ApplicationRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _candidates = MutableStateFlow<List<CandidateUiItem>>(emptyList())
    private val _filteredCandidates = MutableStateFlow<List<CandidateUiItem>>(emptyList())
    val candidates: StateFlow<List<CandidateUiItem>> = _filteredCandidates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadCandidates() {
        val currentCompanyId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val myJobs = jobRepository.getJobPostingsByCompany(currentCompanyId)
                val myJobIds = myJobs.map { it.id }

                if (myJobIds.isNotEmpty()) {
                    val applications = applicationRepository.getApplicationsForJobs(myJobIds)

                    if (applications.isNotEmpty()) {
                        val candidateIds = applications.map { it.candidateId }.distinct()

                        val users = userRepository.getUsersByIds(candidateIds)

                        val uiList = users.map { user ->
                            val count = applications.count { it.candidateId == user.uid }

                            val initials = if (user.name.isNotBlank()) {
                                user.name.split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .joinToString("")
                                    .uppercase()
                            } else {
                                "??"
                            }

                            CandidateUiItem(
                                user = user,
                                applicationsCount = count,
                                hasResume = true,
                                registrationDateFormatted = "14/10/2025",
                                initials = initials
                            )
                        }
                        _candidates.value = uiList
                        _filteredCandidates.value = uiList
                    } else {
                        _candidates.value = emptyList()
                        _filteredCandidates.value = emptyList()
                    }
                } else {
                    _candidates.value = emptyList()
                    _filteredCandidates.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _candidates.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _filteredCandidates.value = _candidates.value
        } else {
            _filteredCandidates.value = _candidates.value.filter {
                it.user.name.contains(query, ignoreCase = true) ||
                        it.user.email.contains(query, ignoreCase = true)
            }
        }
    }
}