package com.edu.muraldetalentosapp.viewmodel

import androidx.lifecycle.ViewModel
import com.edu.muraldetalentosapp.ui.model.JobPosting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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
    val isPostedSuccess: Boolean = false
)

class JobsViewModel : ViewModel() {

    private val _jobs = MutableStateFlow(generateMockJobs())
    val jobs: StateFlow<List<JobPosting>> = _jobs.asStateFlow()

    private val _uiState = MutableStateFlow(PostJobUiState())
    val uiState: StateFlow<PostJobUiState> = _uiState.asStateFlow()

    fun toggleApplication(jobTitle: String) {
        _jobs.update { currentList ->
            currentList.map { job ->
                if (job.title == jobTitle) {
                    // Tratamento seguro para nulos (isApplied é Boolean?)
                    val currentStatus = job.isApplied ?: false
                    job.copy(isApplied = !currentStatus)
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

        val currentTimestamp = System.currentTimeMillis()
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        val expirationTimestamp = currentTimestamp + thirtyDaysInMillis
        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(currentTimestamp))

        val newJob = JobPosting(
            id = UUID.randomUUID().toString(),
            title = state.title,
            company = "Minha Empresa (Demo)",
            description = state.description,
            location = state.location,
            type = state.contractType, // Visual
            contractType = state.contractType, // Lógico
            salaryRange = if (state.isSalaryNegotiable) "A combinar" else state.salary,
            isSalaryNegotiable = state.isSalaryNegotiable,
            publishedAt = formattedDate,
            datePosted = currentTimestamp,
            expirationDate = expirationTimestamp,
            isApplied = false,
            imageUrl = state.imageUrl.ifBlank { null },
            latitude = -4.9685, // Mock Quixadá Centro
            longitude = -39.0150
        )

        _jobs.update { listOf(newJob) + it }
        _uiState.update { PostJobUiState(isPostedSuccess = true) }
    }

    private fun generateMockJobs(): List<JobPosting> {
        val now = System.currentTimeMillis()
        val thirtyDays = 30L * 24 * 60 * 60 * 1000

        return listOf(
            JobPosting(
                id = UUID.randomUUID().toString(),
                title = "Vendedor Interno",
                company = "Loja Magazine",
                description = "Responsável pelo atendimento ao cliente, organização de produtos e vendas internas. Necessário ensino médio completo.",
                type = "CLT",
                contractType = "CLT",
                location = "Quixadá, CE",
                salaryRange = "R$ 1.800 - R$ 2.500",
                isSalaryNegotiable = false,
                publishedAt = "30/09/2025",
                datePosted = now,
                expirationDate = now + thirtyDays,
                isApplied = false,
                latitude = -4.9685,
                longitude = -39.0150
            ),
            JobPosting(
                id = UUID.randomUUID().toString(),
                title = "Repositor de Mercadorias",
                company = "Supermercado Central",
                description = "Reposição de prateleiras, verificação de validade e organização de estoque.",
                type = "CLT",
                contractType = "CLT",
                location = "Quixadá, CE",
                salaryRange = "R$ 1.600 - R$ 2.000",
                isSalaryNegotiable = false,
                publishedAt = "04/10/2025",
                datePosted = now,
                expirationDate = now + thirtyDays,
                isApplied = false,
                latitude = -4.9700,
                longitude = -39.0200
            ),
            JobPosting(
                id = UUID.randomUUID().toString(),
                title = "Desenvolvedor Android Pleno",
                company = "Startup Vision",
                description = "Desenvolvimento de aplicativos nativos utilizando Kotlin e Jetpack Compose. Trabalho remoto.",
                type = "PJ",
                contractType = "PJ",
                location = "Remoto",
                salaryRange = "R$ 7.000 - R$ 9.000",
                isSalaryNegotiable = true,
                publishedAt = "01/10/2025",
                datePosted = now,
                expirationDate = now + thirtyDays,
                isApplied = false,
                latitude = -4.9793,
                longitude = -39.0564
            ),
            JobPosting(
                id = UUID.randomUUID().toString(),
                title = "Auxiliar Administrativo",
                company = "Escritório Contábil Futuro",
                description = "Auxílio nas rotinas do escritório, emissão de notas fiscais e atendimento telefônico.",
                type = "Estágio",
                contractType = "Estágio",
                location = "Quixadá, CE",
                salaryRange = "R$ 800",
                isSalaryNegotiable = false,
                publishedAt = "10/10/2025",
                datePosted = now,
                expirationDate = now + thirtyDays,
                isApplied = false,
                latitude = -4.9750,
                longitude = -39.0400
            ),
            JobPosting(
                id = UUID.randomUUID().toString(),
                title = "Garçom / Garçonete",
                company = "Restaurante Sabor do Sertão",
                description = "Atendimento às mesas, anotar pedidos e servir clientes.",
                type = "CLT",
                contractType = "CLT",
                location = "Quixadá, CE",
                salaryRange = "R$ 1.500 + gorjetas",
                isSalaryNegotiable = false,
                publishedAt = "11/10/2025",
                datePosted = now,
                expirationDate = now + thirtyDays,
                isApplied = false,
                latitude = -4.9650,
                longitude = -39.0100
            ),
            JobPosting(
                id = UUID.randomUUID().toString(),
                title = "Técnico de Enfermagem",
                company = "Hospital Eudásio Barroso",
                description = "Atuar na assistência aos pacientes, administração de medicamentos e cuidados gerais.",
                type = "Concurso",
                contractType = "Concurso",
                location = "Quixadá, CE",
                salaryRange = "R$ 2.200 - R$ 3.000",
                isSalaryNegotiable = false,
                publishedAt = "12/10/2025",
                datePosted = now,
                expirationDate = now + thirtyDays,
                isApplied = false,
                latitude = -4.9720,
                longitude = -39.0250
            ),
            JobPosting(
                id = UUID.randomUUID().toString(),
                title = "Professor de Inglês",
                company = "Escola de Idiomas Wize",
                description = "Ministrar aulas de inglês para turmas iniciantes e intermediárias.",
                type = "Autônomo",
                contractType = "Autônomo",
                location = "Quixadá, CE",
                salaryRange = "R$ 30/hora",
                isSalaryNegotiable = false,
                publishedAt = "13/10/2025",
                datePosted = now,
                expirationDate = now + thirtyDays,
                isApplied = false,
                latitude = -4.9800,
                longitude = -39.0500
            ),
            JobPosting(
                id = UUID.randomUUID().toString(),
                title = "Caixa de Loja",
                company = "Farmácia Pague Menos",
                description = "Operação de caixa, recebimento de valores e abertura/fechamento de caixa.",
                type = "CLT",
                contractType = "CLT",
                location = "Quixadá, CE",
                salaryRange = "R$ 1.412",
                isSalaryNegotiable = false,
                publishedAt = "14/10/2025",
                datePosted = now,
                expirationDate = now + thirtyDays,
                isApplied = false,
                latitude = -4.9690,
                longitude = -39.0180
            )
        )
    }
}