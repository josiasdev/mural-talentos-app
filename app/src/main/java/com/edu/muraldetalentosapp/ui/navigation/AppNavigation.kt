package com.edu.muraldetalentosapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edu.muraldetalentosapp.ui.LoginScreen
import com.edu.muraldetalentosapp.ui.RegisterScreen
import com.edu.muraldetalentosapp.ui.components.AccountType
import com.edu.muraldetalentosapp.ui.screen.HomeScreen
import com.edu.muraldetalentosapp.ui.screen.JobMapScreen
import com.edu.muraldetalentosapp.ui.screen.PostJobScreen
import com.edu.muraldetalentosapp.ui.screen.ProfileScreen
import com.edu.muraldetalentosapp.viewmodel.AuthViewModel
import com.edu.muraldetalentosapp.viewmodel.JobsViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object Home : Screen("home")
    object Map : Screen("map")
    object PostJob: Screen("post_job")
}

@Composable
fun AppNavigation() {
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
                    // Após o registro bem-sucedido, navega para a Home limpando a pilha de navegação
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

                userType = userType
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
    }
}
