package com.edu.muraldetalentosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edu.muraldetalentosapp.ui.LoginScreen
import com.edu.muraldetalentosapp.ui.MockNavigationScreen
import com.edu.muraldetalentosapp.ui.RegisterScreen
import com.edu.muraldetalentosapp.ui.theme.MuralDeTalentosAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuralDeTalentosAPPTheme {
                val navController = rememberNavController()
                val authViewModel: com.edu.muraldetalentosapp.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "mock",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("mock") {
                            MockNavigationScreen(
                                onNavigateToLogin = { navController.navigate("login") },
                                onNavigateToRegister = { navController.navigate("register") },
                                viewModel = authViewModel
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { 
                                    navController.popBackStack()
                                },
                                onNavigateToRegister = { navController.navigate("register") },
                                viewModel = authViewModel
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = { 
                                    navController.navigate("mock") {
                                        popUpTo("mock") { inclusive = true }
                                    }
                                },
                                onNavigateBack = { navController.popBackStack() },
                                viewModel = authViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}