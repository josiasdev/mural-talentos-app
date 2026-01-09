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
    viewModel: AuthViewModel
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current


    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }


    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
            }
            is AuthState.Error -> {
                val message = (authState as AuthState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                        Color(0xFFE0F2FE),
                        Color(0xFFDBEAFE)
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


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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


                    RegisterTextField(
                        label = "Nome Completo",
                        value = name,
                        onValueChange = { 
                            name = it
                            if (nameError != null) nameError = null
                        },
                        placeholder = "João Silva",
                        errorMessage = nameError
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))


                    RegisterTextField(
                        label = "E-mail",
                        value = email,
                        onValueChange = { 
                            email = it
                            if (emailError != null) emailError = null
                        },
                        placeholder = "seu@email.com",
                        errorMessage = emailError
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    RegisterTextField(
                        label = "Telefone",
                        value = phone,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(11)
                            var formatted = ""
                            if (digits.isNotEmpty()) {
                                formatted = "(" + digits.substring(0, minOf(2, digits.length))
                                if (digits.length > 2) {
                                    formatted += ") " + digits.substring(2, minOf(7, digits.length))
                                    if (digits.length > 7) {
                                        formatted += "-" + digits.substring(7, digits.length)
                                    }
                                }
                            }
                            phone = formatted
                            if (phoneError != null) phoneError = null
                        },
                        placeholder = "(11) 99999-9999",
                        errorMessage = phoneError
                    )

                    Spacer(modifier = Modifier.height(16.dp))


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


                    RegisterTextField(
                        label = "Senha",
                        value = password,
                        onValueChange = { 
                            password = it
                            if (passwordError != null) passwordError = null
                        },
                        placeholder = "••••••••",
                        isPassword = true,
                        errorMessage = passwordError
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    RegisterTextField(
                        label = "Confirmar Senha",
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            if (confirmPasswordError != null) confirmPasswordError = null
                        },
                        placeholder = "••••••••",
                        isPassword = true,
                        errorMessage = confirmPasswordError
                    )

                    Spacer(modifier = Modifier.height(24.dp))


                    Button(
                        onClick = { 

                            var isValid = true
                            
                            if (name.isBlank()) {
                                nameError = "Nome é obrigatório"
                                isValid = false
                            }
                            
                            if (email.isBlank()) {
                                emailError = "E-mail é obrigatório"
                                isValid = false
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "E-mail inválido"
                                isValid = false
                            }
                            
                            if (phone.isBlank()) {
                                phoneError = "Telefone é obrigatório"
                                isValid = false
                            }
                            
                            if (password.isBlank()) {
                                passwordError = "Senha é obrigatória"
                                isValid = false
                            } else if (password.length < 6) {
                                passwordError = "A senha deve ter pelo menos 6 caracteres"
                                isValid = false
                            }
                            
                            if (confirmPassword.isBlank()) {
                                confirmPasswordError = "Confirmação de senha é obrigatória"
                                isValid = false
                            } else if (password != confirmPassword) {
                                confirmPasswordError = "As senhas não coincidem"
                                isValid = false
                            }

                            if (isValid) {
                                viewModel.signUp(email, password, name)
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
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun RegisterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                unfocusedTextColor = Color.Black,
                errorContainerColor = Color(0xFFFFF0F0),
                errorIndicatorColor = Color.Red
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            singleLine = true,
            isError = errorMessage != null
        )
        
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview
@Composable
fun RegisterScreenPreview() {
    // RegisterScreen(viewModel = AuthViewModel())
}
