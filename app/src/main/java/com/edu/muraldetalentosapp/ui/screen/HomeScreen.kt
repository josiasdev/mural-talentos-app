package com.edu.muraldetalentosapp.ui.screen


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.edu.muraldetalentosapp.ui.theme.BluePrimary
import com.edu.muraldetalentosapp.ui.theme.TextGray
import com.edu.muraldetalentosapp.viewmodel.JobsViewModel
import com.edu.muraldetalentosapp.ui.components.AccountType
import androidx.compose.material.icons.filled.DarkMode // Ícone Lua
import androidx.compose.material.icons.filled.LightMode // Ícone Sol



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToPostJob: () -> Unit = {},
    onNavigateToSearchCandidates: () -> Unit,
    userType: AccountType?,
    viewModel: JobsViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.fetchJobs()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mural de Talentos", color = BluePrimary, fontWeight = FontWeight.Bold)
                        if (userType == AccountType.COMPANY) {
                            Text("Empresa", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sair",
                            tint = BluePrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Alternar Tema",
                            tint = BluePrimary
                        )
                    }
                    if (userType == AccountType.CANDIDATE) {
                        IconButton(onClick = onNavigateToMap) {
                            Icon(Icons.Default.Map, contentDescription = "Mapa", tint = BluePrimary)
                        }
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.Default.Person, contentDescription = "Perfil", tint = BluePrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {
            if (userType == AccountType.COMPANY) {
                CompanyDashboard(
                    viewModel = viewModel,
                    onNavigateToPostJob = onNavigateToPostJob,
                    onNavigateToSearchCandidates = onNavigateToSearchCandidates
                )
            } else {
                CandidateFeedContent(
                    viewModel = viewModel,
                    onNavigateToMap = onNavigateToMap
                )
            }
        }
    }
}
