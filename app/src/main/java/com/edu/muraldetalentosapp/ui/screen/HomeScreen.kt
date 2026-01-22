package com.edu.muraldetalentosapp.ui.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edu.muraldetalentosapp.ui.theme.BluePrimary
import com.edu.muraldetalentosapp.viewmodel.JobsViewModel
import com.edu.muraldetalentosapp.ui.components.AccountType
import androidx.compose.material.icons.filled.DarkMode // Ícone Lua
import androidx.compose.material.icons.filled.LightMode // Ícone Sol
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.edu.muraldetalentosapp.viewmodel.NotificationViewModel
import com.edu.muraldetalentosapp.ui.components.NotificationBell
import com.edu.muraldetalentosapp.ui.components.NotificationListSheet
import org.koin.androidx.compose.koinViewModel



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToPostJob: () -> Unit = {},
    onNavigateToSearchCandidates: () -> Unit,
    onNavigateToCandidates: (String, String) -> Unit,
    userType: AccountType?,
    viewModel: JobsViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    notificationViewModel: NotificationViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchJobs()
    }

    // Observa se há alerta de aplicação (usuário não completou cadastro)
    val applyAlert by viewModel.applyAlert.collectAsState()

    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val notifications by notificationViewModel.notifications.collectAsState()
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        NotificationListSheet(
            notifications = notifications,
            onDismiss = { showSheet = false },
            onMarkAsRead = { notificationViewModel.markAsRead(it) },
            onMarkAllAsRead = { notificationViewModel.markAllAsRead() }
        )
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
                        NotificationBell(
                            unreadCount = unreadCount,
                            onClick = { showSheet = true }
                        )
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

            Column {
                // Banner de alerta vermelho, aparece quando applyAlert não for nulo
                if (applyAlert != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFB00020))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = applyAlert ?: "",
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(onClick = {
                            // Navega para perfil para completar cadastro e limpa o alerta
                            onNavigateToProfile()
                            viewModel.clearApplyAlert()
                        }) {
                            Text("Completar", color = Color.White)
                        }
                    }
                }

                if (userType == AccountType.COMPANY) {
                    CompanyDashboard(
                        viewModel = viewModel,
                        onNavigateToPostJob = onNavigateToPostJob,
                        onNavigateToSearchCandidates = onNavigateToSearchCandidates,
                        onNavigateToCandidates = onNavigateToCandidates
                    )
                } else {
                    CandidateFeedContent(
                        viewModel = viewModel
                    )
                }
             }
         }
     }
 }
