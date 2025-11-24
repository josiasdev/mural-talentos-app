package com.edu.muraldetalentosapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.muraldetalentosapp.ui.components.AccountType
import com.edu.muraldetalentosapp.ui.components.AccountTypeButton
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import com.edu.muraldetalentosapp.viewmodel.AuthViewModel
import com.edu.muraldetalentosapp.viewmodel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
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
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color(0xFF0A0A0A)
                    )
                }
                Text(
                    text = "Criar Conta",
                    fontSize = 18.sp,
                    color = Color(0xFF193CB8),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Registration Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Fill remaining space but allow scrolling inside if needed
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Cadastro",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "Preencha os dados para criar sua conta",
                        fontSize = 14.sp,
                        color = Color(0xFF717182),
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    // Account Type Toggle
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
                    RegisterFormFields(viewModel, authState)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun RegisterFormFields(viewModel: AuthViewModel, authState: AuthState) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Name
    RegisterTextField(
        label = "Nome Completo",
        value = name,
        onValueChange = { name = it },
        placeholder = "João Silva"
    )
    
    Spacer(modifier = Modifier.height(16.dp))

    // Email
    RegisterTextField(
        label = "E-mail",
        value = email,
        onValueChange = { email = it },
        placeholder = "seu@email.com"
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Phone
    RegisterTextField(
        label = "Telefone",
        value = phone,
        onValueChange = { phone = it },
        placeholder = "(11) 99999-9999"
    )

    Spacer(modifier = Modifier.height(16.dp))

    // About
    Text(
        text = "Sobre Você",
        fontSize = 14.sp,
        color = Color(0xFF0A0A0A),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    TextField(
        value = about,
        onValueChange = { about = it },
        placeholder = { Text("Conte um pouco sobre sua experiência...", color = Color(0xFF717182)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
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
        maxLines = 4
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Password
    RegisterTextField(
        label = "Senha",
        value = password,
        onValueChange = { password = it },
        placeholder = "••••••••",
        isPassword = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Confirm Password
    RegisterTextField(
        label = "Confirmar Senha",
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        placeholder = "••••••••",
        isPassword = true
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Create Account Button
    Button(
        onClick = { 
            if (password == confirmPassword) {
                viewModel.signUp(email, password) 
            } else {
                // Handle password mismatch (could show a toast or error text)
            }
        },
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
            text = if (authState is AuthState.Loading) "Criando Conta..." else "Criar Conta",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RegisterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    Text(
        text = label,
        fontSize = 14.sp,
        color = Color(0xFF0A0A0A),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFF717182)) },
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
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        singleLine = true
    )
}

@Preview
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}
