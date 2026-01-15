package com.edu.muraldetalentosapp.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.muraldetalentosapp.ui.components.JobCard
import com.edu.muraldetalentosapp.ui.theme.BackgroundGray
import com.edu.muraldetalentosapp.ui.theme.BluePrimary
import com.edu.muraldetalentosapp.ui.theme.TextGray
import com.edu.muraldetalentosapp.viewmodel.JobsViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CandidateFeedContent(
    viewModel: JobsViewModel,
    onNavigateToMap: () -> Unit
) {
    val jobs by viewModel.jobs.collectAsState()

    var search by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isFilterExpanded by remember { mutableStateOf(false) }

    val jobTypes = remember(jobs) { jobs.map { it.type }.distinct() }
    val locations = remember(jobs) { jobs.map { it.location }.distinct() }

    var selectedLocations by remember { mutableStateOf(emptySet<String>()) }
    var selectedJobTypes by remember { mutableStateOf(emptySet<String>()) }

    val salaryRange = 0f..10000f
    var selectedSalaryRange by remember { mutableStateOf(salaryRange) }

    fun parseSalary(salary: String?): Float? {
        if (salary == null || salary == "A combinar") return null
        val salaryString = salary.split("-")[0]
        val cleanedSalary = salaryString.replace(Regex("[^0-9,]"), "")
        return cleanedSalary.replace(',', '.').toFloatOrNull()
    }

    val filteredJobs = remember(jobs, search, selectedLocations, selectedJobTypes, selectedSalaryRange) {
        jobs.filter { job ->
            val searchMatch = if (search.isBlank()) true else {
                job.title.contains(search, ignoreCase = true) || job.company.contains(search, ignoreCase = true)
            }
            val locationMatch = if (selectedLocations.isEmpty()) true else {
                selectedLocations.contains(job.location)
            }
            val typeMatch = if (selectedJobTypes.isEmpty()) true else {
                selectedJobTypes.contains(job.type)
            }
            val salaryValue = parseSalary(job.salaryRange)
            val salaryMatch = if (salaryValue == null) true else {
                salaryValue >= selectedSalaryRange.start && salaryValue <= selectedSalaryRange.endInclusive
            }

            searchMatch && locationMatch && typeMatch && salaryMatch
        }
    }

    val availableJobs = filteredJobs.filter { !it.isApplied }
    val appliedJobs = filteredJobs.filter { it.isApplied }

    val tabs = listOf("Todas (${availableJobs.size})", "Candidaturas (${appliedJobs.size})")

    val displayedJobs = when (selectedTab) {
        0 -> availableJobs
        1 -> appliedJobs
        else -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            TextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar vagas...", color = MaterialTheme.colorScheme.onSurfaceVariant ) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )
            IconButton(onClick = { isFilterExpanded = !isFilterExpanded }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtrar",
                    tint = if (isFilterExpanded) BluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(visible = isFilterExpanded) {
            Column(Modifier.padding(vertical = 16.dp)) {
                Text("Localização", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp), color = MaterialTheme.colorScheme.onBackground)
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
                            label = { Text(location) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary.copy(alpha = 0.1f),
                                selectedLabelColor = BluePrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Tipo de Vaga", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp), color = MaterialTheme.colorScheme.onBackground)
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
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary.copy(alpha = 0.1f),
                                selectedLabelColor = BluePrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Faixa Salarial", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp), color = MaterialTheme.colorScheme.onBackground)
                RangeSlider(
                    value = selectedSalaryRange,
                    onValueChange = { selectedSalaryRange = it },
                    valueRange = salaryRange,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = BluePrimary,
                        activeTrackColor = BluePrimary
                    )
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("R$ ${selectedSalaryRange.start.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("R$ ${selectedSalaryRange.endInclusive.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BluePrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BluePrimary
                )
            }
        ) {
            tabs.forEachIndexed { index, text ->
                Tab(
                    selected = index == selectedTab,
                    onClick = { selectedTab = index },
                    text = { Text(text, fontWeight = if(index == selectedTab) FontWeight.Bold else FontWeight.Normal) },
                    selectedContentColor = BluePrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(displayedJobs) { job ->
                JobCard(
                    job = job,
                    onClick = {
                        viewModel.applyToJob(job.id)
                    }
                )
            }

            if (displayedJobs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhuma vaga encontrada.", color = TextGray)
                    }
                }
            }
        }
    }
}
