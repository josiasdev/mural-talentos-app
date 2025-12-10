package com.edu.muraldetalentosapp.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.edu.muraldetalentosapp.ui.model.JobVacancy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class JobEntryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()

    private val _contractType = MutableStateFlow("")
    val contractType = _contractType.asStateFlow()

    private val _isSalaryNegotiable = MutableStateFlow(false)
    val isSalaryNegotiable = _isSalaryNegotiable.asStateFlow()

    private val _salaryRange = MutableStateFlow("")
    val salaryRange = _salaryRange.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onTitleChange(newValue: String) { _title.value = newValue }
    fun onDescriptionChange(newValue: String) { _description.value = newValue }
    fun onLocationChange(newValue: String) { _location.value = newValue }
    fun onContractTypeChange(newValue: String) { _contractType.value = newValue }
    fun onSalaryNegotiableChange(newValue: Boolean) { _isSalaryNegotiable.value = newValue }
    fun onSalaryRangeChange(newValue: String) { _salaryRange.value = newValue }

    fun publishJob(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_title.value.isBlank() || _description.value.isBlank() || _location.value.isBlank()) {
            onError("Preencha os campos obrigatórios (*)")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val newJob = JobVacancy(
                title = _title.value,
                description = _description.value,
                location = _location.value,
                contractType = _contractType.value,
                isSalaryNegotiable = _isSalaryNegotiable.value,
                salaryRange = if (_isSalaryNegotiable.value) "A combinar" else _salaryRange.value,
                userId = "user_id_temp" // Substituir pelo Auth ID real depois
            )

            db.collection("vagas")
                .add(newJob)
                .addOnSuccessListener {
                    _isLoading.value = false
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    _isLoading.value = false
                    onError(e.message ?: "Erro ao salvar")
                }
        }
    }
}