package com.edu.muraldetalentosapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edu.muraldetalentosapp.ui.screen.LoginScreen
import com.edu.muraldetalentosapp.ui.screen.RegisterScreen
import com.edu.muraldetalentosapp.ui.screen.HomeScreen
import com.edu.muraldetalentosapp.ui.screen.JobMapScreen
import com.edu.muraldetalentosapp.ui.screen.PostJobScreen
import com.edu.muraldetalentosapp.ui.screen.ProfileScreen
import com.edu.muraldetalentosapp.ui.screen.SearchCandidatesScreen
import com.edu.muraldetalentosapp.viewmodel.AuthViewModel
import com.edu.muraldetalentosapp.viewmodel.JobsViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object Home : Screen("home")
    object Map : Screen("map")
    object PostJob: Screen("post_job")
    object SearchCandidates: Screen("search_candidates")
    object CandidateList : Screen("candidate_list/{jobId}/{jobTitle}") {
        fun createRoute(jobId: String, jobTitle: String) = "candidate_list/$jobId/$jobTitle"
    }
}

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val jobsViewModel: JobsViewModel = viewModel()
    val userType by authViewModel.userType.collectAsState()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onNavigateBack = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
                viewModel = jobsViewModel,
                onNavigateToPostJob = {
                    navController.navigate(Screen.PostJob.route)
                },
                onNavigateToSearchCandidates = {
                    navController.navigate(Screen.SearchCandidates.route)
                },
                onNavigateToCandidates = { jobId, jobTitle ->
                    navController.navigate(Screen.CandidateList.createRoute(jobId, jobTitle))
                },

                userType = userType,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }

        composable(Screen.Map.route) {
            JobMapScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = jobsViewModel
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route)
                })
        }

        composable(Screen.PostJob.route) {
            PostJobScreen(
                viewModel = jobsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SearchCandidates.route) {
            SearchCandidatesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CandidateList.route,
            arguments = listOf(
                androidx.navigation.navArgument("jobId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("jobTitle") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            val jobTitle = backStackEntry.arguments?.getString("jobTitle") ?: "Candidatos"

            com.edu.muraldetalentosapp.ui.screen.CandidateListScreen(
                jobId = jobId,
                jobTitle = jobTitle,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
