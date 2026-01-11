package com.edu.muraldetalentosapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edu.muraldetalentosapp.ui.LoginScreen
import com.edu.muraldetalentosapp.ui.RegisterScreen
import com.edu.muraldetalentosapp.ui.screen.HomeScreen
import com.edu.muraldetalentosapp.ui.screen.JobMapScreen
import com.edu.muraldetalentosapp.ui.screen.ProfileScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object Home : Screen("home")
    object Map : Screen("map")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: com.edu.muraldetalentosapp.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val jobsViewModel: com.edu.muraldetalentosapp.viewmodel.JobsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
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
                    navController.popBackStack()
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
                viewModel = jobsViewModel
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
    }
}
