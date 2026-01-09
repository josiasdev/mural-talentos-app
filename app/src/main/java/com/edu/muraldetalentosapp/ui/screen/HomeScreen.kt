package com.edu.muraldetalentosapp.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.muraldetalentosapp.ui.components.JobCard
import com.edu.muraldetalentosapp.ui.model.JobPosting

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(onNavigateToProfile: () -> Unit = {}) {

    var search by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isFilterExpanded by remember { mutableStateOf(false) }

    var jobs by remember {
        mutableStateOf(
            listOf(
                JobPosting(
                    title = "Vendedor Interno",
                    company = "Loja Magazine",
                    type = "CLT",
                    location = "Quixadá, CE",
                    salaryRange = "R$ 1.800 - R$ 2.500",
                    publishedAt = "30/09/2025"
                ),
                JobPosting(
                    title = "Repositor de Mercadorias",
                    company = "Supermercado Central",
                    type = "CLT",
                    location = "Quixadá, CE",
                    salaryRange = "R$ 1.600 - R$ 2.000",
                    publishedAt = "04/10/2025"
                ),
                JobPosting(
                    title = "Desenvolvedor Android Pleno",
                    company = "Startup Vision",
                    type = "PJ",
                    location = "Remoto",
                    salaryRange = "R$ 7.000 - R$ 9.000",
                    publishedAt = "01/10/2025"
                ),
                JobPosting(
                    title = "Auxiliar Administrativo",
                    company = "Escritório Contábil Futuro",
                    type = "Estágio",
                    location = "Quixadá, CE",
                    salaryRange = "R$ 800",
                    publishedAt = "10/10/2025"
                ),
                JobPosting(
                    title = "Garçom / Garçonete",
                    company = "Restaurante Sabor do Sertão",
                    type = "CLT",
                    location = "Quixadá, CE",
                    salaryRange = "R$ 1.500 + gorjetas",
                    publishedAt = "11/10/2025"
                ),
                JobPosting(
                    title = "Técnico de Enfermagem",
                    company = "Hospital Eudásio Barroso",
                    type = "Concurso",
                    location = "Quixadá, CE",
                    salaryRange = "R$ 2.200 - R$ 3.000",
                    publishedAt = "12/10/2025"
                ),
                JobPosting(
                    title = "Professor de Inglês",
                    company = "Escola de Idiomas Wize",
                    type = "Autônomo",
                    location = "Quixadá, CE",
                    salaryRange = "R$ 30/hora",
                    publishedAt = "13/10/2025"
                ),
                JobPosting(
                    title = "Caixa de Loja",
                    company = "Farmácia Pague Menos",
                    type = "CLT",
                    location = "Quixadá, CE",
                    salaryRange = "R$ 1.412",
                    publishedAt = "14/10/2025"
                )
            )
        )
    }

    val jobTypes = jobs.map { it.type }.distinct()
    val locations = jobs.map { it.location }.distinct()

    var selectedLocations by remember { mutableStateOf(emptySet<String>()) }
    var selectedJobTypes by remember { mutableStateOf(emptySet<String>()) }
    val salaryRange = 0f..10000f
    var selectedSalaryRange by remember { mutableStateOf(salaryRange) }


    fun parseSalary(salary: String): Float? {
        val salaryString = salary.split("-")[0]
        val cleanedSalary = salaryString.replace(Regex("[^0-9,]"), "")
        return cleanedSalary.replace(',', '.').toFloatOrNull()
    }

    val filteredJobs = jobs.filter { job ->
        val searchMatch = if (search.isBlank()) true else {
            job.title.contains(search, ignoreCase = true) || job.company.contains(search, ignoreCase = true)
        }
        val locationMatch = if (selectedLocations.isEmpty()) true else {
            selectedLocations.contains(job.location)
        }
        val typeMatch = if (selectedJobTypes.isEmpty()) true else {
            selectedJobTypes.contains(job.type)
        }
        val salaryMatch = parseSalary(job.salaryRange)?.let {
            it >= selectedSalaryRange.start && it <= selectedSalaryRange.endInclusive
        } ?: true

        searchMatch && locationMatch && typeMatch && salaryMatch
    }

    val availableJobs = filteredJobs.filter { !it.isApplied }
    val appliedJobs = filteredJobs.filter { it.isApplied }

    val allJobsCount = availableJobs.size
    val appliedJobsCount = appliedJobs.size
    val tabs = listOf("Todas ($allJobsCount)", "Candidaturas ($appliedJobsCount)")

    val onApplyClick = { clickedJob: JobPosting ->
        jobs = jobs.map {
            if (it.title == clickedJob.title) it.copy(isApplied = true) else it
        }
    }

    val displayedJobs = when (selectedTab) {
        0 -> availableJobs
        1 -> appliedJobs
        else -> emptyList()
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Mural de Talentos", color = BluePrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = BluePrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = BluePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar vagas...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BackgroundGray,
                        unfocusedContainerColor = BackgroundGray,
                        disabledContainerColor = BackgroundGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                IconButton(onClick = { isFilterExpanded = !isFilterExpanded }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filtrar", tint = BluePrimary)
                }
            }

            AnimatedVisibility(visible = isFilterExpanded) {
                Column(Modifier.padding(vertical = 16.dp)) {
                    Text("Localização", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        locations.forEach { location ->
                            FilterChip(
                                selected = selectedLocations.contains(location),
                                onClick = {
                                    selectedLocations = if (selectedLocations.contains(location)) {
                                        selectedLocations - location
                                    } else {
                                        selectedLocations + location
                                    }
                                },
                                label = { Text(location) }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Tipo de Vaga", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        jobTypes.forEach { type ->
                            FilterChip(
                                selected = selectedJobTypes.contains(type),
                                onClick = {
                                    selectedJobTypes = if (selectedJobTypes.contains(type)) {
                                        selectedJobTypes - type
                                    } else {
                                        selectedJobTypes + type
                                    }
                                },
                                label = { Text(type) }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Faixa Salarial", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    RangeSlider(
                        value = selectedSalaryRange,
                        onValueChange = { selectedSalaryRange = it },
                        valueRange = salaryRange,
                        steps = 9
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("R$ ${selectedSalaryRange.start.toInt()}", fontSize = 12.sp, color = TextGray)
                        Text("R$ ${selectedSalaryRange.endInclusive.toInt()}", fontSize = 12.sp, color = TextGray)
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BluePrimary
            ) {
                tabs.forEachIndexed { index, text ->
                    Tab(
                        selected = index == selectedTab,
                        onClick = { selectedTab = index },
                        text = { Text(text) },
                        selectedContentColor = BluePrimary,
                        unselectedContentColor = TextGray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(displayedJobs) { job ->
                    JobCard(job = job, onClick = { onApplyClick(job) })
                }
            }
        }
    }
}