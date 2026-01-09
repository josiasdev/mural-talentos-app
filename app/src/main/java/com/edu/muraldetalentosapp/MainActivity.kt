package com.edu.muraldetalentosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.edu.muraldetalentosapp.ui.navigation.AppNavigation
import com.edu.muraldetalentosapp.ui.theme.MuralDeTalentosAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuralDeTalentosAPPTheme {
                AppNavigation()
            }
        }
    }
}
