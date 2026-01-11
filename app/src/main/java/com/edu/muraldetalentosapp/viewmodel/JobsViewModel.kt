package com.edu.muraldetalentosapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.edu.muraldetalentosapp.ui.model.JobPosting

class JobsViewModel : ViewModel() {

    var jobs by mutableStateOf(generateMockJobs())
        private set

    fun toggleApplication(jobTitle: String) {
        jobs = jobs.map {
            if (it.title == jobTitle) it.copy(isApplied = !it.isApplied) else it
        }
    }

    private fun generateMockJobs(): List<JobPosting> {
        
        return listOf(
            JobPosting(
                title = "Vendedor Interno",
                company = "Loja Magazine",
                type = "CLT",
                location = "Quixadá, CE",
                salaryRange = "R$ 1.800 - R$ 2.500",
                publishedAt = "30/09/2025",
                latitude = -4.9685,
                longitude = -39.0150
            ),
            JobPosting(
                title = "Repositor de Mercadorias",
                company = "Supermercado Central",
                type = "CLT",
                location = "Quixadá, CE",
                salaryRange = "R$ 1.600 - R$ 2.000",
                publishedAt = "04/10/2025",
                latitude = -4.9700,
                longitude = -39.0200
            ),
            JobPosting(
                title = "Desenvolvedor Android Pleno",
                company = "Startup Vision",
                type = "PJ",
                location = "Remoto",
                salaryRange = "R$ 7.000 - R$ 9.000",
                publishedAt = "01/10/2025",
                latitude = -4.9793, // UFC itself
                longitude = -39.0564
            ),
            JobPosting(
                title = "Auxiliar Administrativo",
                company = "Escritório Contábil Futuro",
                type = "Estágio",
                location = "Quixadá, CE",
                salaryRange = "R$ 800",
                publishedAt = "10/10/2025",
                latitude = -4.9750,
                longitude = -39.0400
            ),
            JobPosting(
                title = "Garçom / Garçonete",
                company = "Restaurante Sabor do Sertão",
                type = "CLT",
                location = "Quixadá, CE",
                salaryRange = "R$ 1.500 + gorjetas",
                publishedAt = "11/10/2025",
                latitude = -4.9650,
                longitude = -39.0100
            ),
            JobPosting(
                title = "Técnico de Enfermagem",
                company = "Hospital Eudásio Barroso",
                type = "Concurso",
                location = "Quixadá, CE",
                salaryRange = "R$ 2.200 - R$ 3.000",
                publishedAt = "12/10/2025",
                latitude = -4.9720,
                longitude = -39.0250
            ),
            JobPosting(
                title = "Professor de Inglês",
                company = "Escola de Idiomas Wize",
                type = "Autônomo",
                location = "Quixadá, CE",
                salaryRange = "R$ 30/hora",
                publishedAt = "13/10/2025",
                latitude = -4.9800,
                longitude = -39.0500
            ),
            JobPosting(
                title = "Caixa de Loja",
                company = "Farmácia Pague Menos",
                type = "CLT",
                location = "Quixadá, CE",
                salaryRange = "R$ 1.412",
                publishedAt = "14/10/2025",
                latitude = -4.9690,
                longitude = -39.0180
            )
        )
    }
}
