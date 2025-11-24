package com.edu.muraldetalentosapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.edu.muraldetalentosapp.ui.components.AccountType
import com.edu.muraldetalentosapp.ui.components.AccountTypeButton
import com.edu.muraldetalentosapp.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import com.edu.muraldetalentosapp.viewmodel.AuthViewModel
import com.edu.muraldetalentosapp.viewmodel.AuthState

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
                viewModel.resetState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0F2FE), // Light blueish white approximation
                        Color(0xFFDBEAFE)  // Slightly darker blue approximation
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Mural de Talentos Logo",
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mural de Talentos",
                color = Color(0xFF193CB8),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Conectando talentos e oportunidades",
                color = Color(0xFF717182),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Entrar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "Faça login para continuar",
                        fontSize = 14.sp,
                        color = Color(0xFF717182),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Toggle Button
                    Text(
                        text = "Tipo de Conta",
                        fontSize = 14.sp,
                        color = Color(0xFF0A0A0A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    var selectedAccountType by remember { mutableStateOf(AccountType.CANDIDATE) }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AccountTypeButton(
                            text = "Candidato",
                            icon = Icons.Outlined.Person,
                            isSelected = selectedAccountType == AccountType.CANDIDATE,
                            onClick = { selectedAccountType = AccountType.CANDIDATE },
                            modifier = Modifier.weight(1f)
                        )
                        AccountTypeButton(
                            text = "Empresa",
                            icon = Icons.Outlined.AccountBox,
                            isSelected = selectedAccountType == AccountType.COMPANY,
                            onClick = { selectedAccountType = AccountType.COMPANY },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Form Fields
                    var email by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }

                    // Email
                    Text(
                        text = "E-mail",
                        fontSize = 14.sp,
                        color = Color(0xFF0A0A0A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("seu@email.com", color = Color(0xFF717182)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3F3F5),
                            unfocusedContainerColor = Color(0xFFF3F3F5),
                            disabledContainerColor = Color(0xFFF3F3F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    Text(
                        text = "Senha",
                        fontSize = 14.sp,
                        color = Color(0xFF0A0A0A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", color = Color(0xFF717182)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3F3F5),
                            unfocusedContainerColor = Color(0xFFF3F3F5),
                            disabledContainerColor = Color(0xFFF3F3F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Buttons
                    Button(
                        onClick = { viewModel.signIn(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF193CB8),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (authState is AuthState.Loading) "Entrando..." else "Entrar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    androidx.compose.material3.TextButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Não tem conta? Cadastre-se",
                            color = Color(0xFF193CB8),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}
