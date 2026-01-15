package com.edu.muraldetalentosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.edu.muraldetalentosapp.ui.navigation.AppNavigation
import com.edu.muraldetalentosapp.ui.theme.MuralDeTalentosAPPTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializa o Firebase explicitamente para evitar o erro de inicialização
        FirebaseApp.initializeApp(this)
        
        enableEdgeToEdge()
        setContent {
            MuralDeTalentosAPPTheme {
                AppNavigation()
            }
        }
    }
}
