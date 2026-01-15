package com.edu.muraldetalentosapp.ui.screen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edu.muraldetalentosapp.ui.theme.BluePrimary
import com.edu.muraldetalentosapp.ui.theme.TextGray
import com.edu.muraldetalentosapp.ui.theme.IconBlue
import com.edu.muraldetalentosapp.ui.theme.BackgroundGray
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --- UI STATE AND VIEWMODEL ---

enum class UploadState {
    Idle, Uploading, Success, Error
}

data class ProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val cpf: String = "",
    val fileName: String? = null,
    val selectedFileUri: Uri? = null,
    val uploadState: UploadState = UploadState.Idle,
    val emailError: String? = null,
    val cpfError: String? = null
)

class ProfileViewModel : ViewModel() {
    var uiState by mutableStateOf(ProfileUiState())
        private set

    fun onNameChange(newValue: String) { uiState = uiState.copy(fullName = newValue) }
    fun onEmailChange(newValue: String) {
        uiState = uiState.copy(email = newValue)
        validateEmail()
    }
    fun onCpfChange(newValue: String) {
        if (newValue.all { it.isDigit() } && newValue.length <= 11) {
            uiState = uiState.copy(cpf = newValue)
            validateCpf()
        }
    }

    fun onFileSelected(uri: Uri, fileName: String) {
        uiState = uiState.copy(selectedFileUri = uri, fileName = fileName, uploadState = UploadState.Idle)
    }

    private fun validateEmail() {
        val email = uiState.email
        val error = if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "E-mail inválido"
        } else {
            null
        }
        uiState = uiState.copy(emailError = error)
    }

    private fun validateCpf() {
        val cpf = uiState.cpf
        val error = if (cpf.isNotEmpty() && cpf.length != 11) {
            "CPF deve ter 11 dígitos"
        } else {
            null
        }
        uiState = uiState.copy(cpfError = error)
    }

    fun saveData(context: Context) {
        validateEmail()
        validateCpf()
        if (uiState.emailError == null && uiState.cpfError == null) {
            val fileUri = uiState.selectedFileUri
            val fileName = uiState.fileName

            if (fileUri != null && fileName != null) {
                uploadResume(context, fileUri, fileName)
            } else {
                uiState = uiState.copy(uploadState = UploadState.Error, fileName = "Por favor, selecione um arquivo de currículo.")
            }
        }
    }

    private fun uploadResume(context: Context, fileUri: Uri, fileName: String) {
        // Limpa caracteres especiais do email para usar no path se necessário, mas @ e . costumam ser aceitos
        val userId = uiState.email.replace(Regex("[^a-zA-Z0-9]"), "_")
        if (userId.isEmpty()) {
            uiState = uiState.copy(uploadState = UploadState.Error, fileName = "E-mail obrigatório para o upload.")
            return
        }

        uiState = uiState.copy(uploadState = UploadState.Uploading)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileBytes = context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    inputStream.readBytes()
                } ?: throw Exception("Não foi possível ler o arquivo selecionado.")

                // O path deve ser relativo ao bucket. Se o bucket é "resumes", o path começa após ele.
                val storagePath = "${userId}/${fileName}"

                com.edu.muraldetalentosapp.data.supabase.SupabaseManager.client.storage.from("resumes").upload(storagePath, fileBytes, upsert = true)

                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(
                        uploadState = UploadState.Success,
                        selectedFileUri = null,
                        fileName = "Enviado com sucesso: $fileName"
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(
                        uploadState = UploadState.Error,
                        fileName = "Erro no upload: ${e.localizedMessage ?: "Causa desconhecida"}"
                    )
                }
            }
        }
    }
}

// --- COMPOSE SCREEN ---

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onNavigateToHome: () -> Unit = {}
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(context, it)
            viewModel.onFileSelected(it, fileName)
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            SimpleTopBar(onBackClick = onBackClick)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(
                icon = Icons.Outlined.Person,
                title = "Dados Pessoais",
                subtitle = "Informações básicas do candidato"
            ) {
                CustomTextField(
                    label = "Nome Completo *",
                    value = state.fullName,
                    onValueChange = viewModel::onNameChange,
                    placeholder = "Digite seu nome completo"
                )
                CustomTextField(
                    label = "E-mail *",
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "seu.email@exemplo.com",
                    keyboardType = KeyboardType.Email,
                    isError = state.emailError != null,
                    errorMessage = state.emailError
                )
                CustomTextField(
                    label = "CPF *",
                    value = state.cpf,
                    onValueChange = viewModel::onCpfChange,
                    placeholder = "000.000.000-00",
                    keyboardType = KeyboardType.Number,
                    isError = state.cpfError != null,
                    errorMessage = state.cpfError
                )
            }

            SectionCard(
                icon = Icons.Outlined.Description,
                title = "Currículo",
                subtitle = "Faça upload do seu currículo em PDF"
            ) {
                Text(
                    text = "Arquivo do Currículo (PDF) *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                UploadBox(
                    fileName = state.fileName,
                    uploadState = state.uploadState,
                    onUploadClick = { launcher.launch("application/pdf") }
                )

                Text(
                    text = "Tamanho máximo: 5MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveData(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = Color.White
                ),
                enabled = state.uploadState != UploadState.Uploading && state.emailError == null && state.cpfError == null
            ) {
                when (state.uploadState) {
                    UploadState.Uploading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviando...", fontSize = 16.sp)
                    }
                    UploadState.Success -> {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviado!", fontSize = 16.sp)
                    }
                    else -> {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salvar Dados", fontSize = 16.sp)
                    }
                }
            }

            Text(
                text = "* Campos obrigatórios",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = TextGray,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex != -1) {
                    result = cursor.getString(columnIndex)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result ?: "unknown"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Meu Perfil",
                    style = MaterialTheme.typography.titleMedium,
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dados do candidato",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun SectionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = IconBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                modifier = Modifier.padding(start = 32.dp, bottom = 16.dp)
            )

            content()
        }
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.padding(bottom = if (isError) 4.dp else 16.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextGray) },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.LightGray,
                    shape = RoundedCornerShape(8.dp)
                ),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = BackgroundGray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            isError = isError
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
            )
        }
    }
}

private data class UploadStateConfig(
    val backgroundColor: Color,
    val textColor: Color,
    val iconTint: Color,
    val contentText: String
)

@Composable
fun UploadBox(
    fileName: String?,
    uploadState: UploadState,
    onUploadClick: () -> Unit
) {
    val stroke = Stroke(
        width = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )

    val config = when (uploadState) {
        UploadState.Uploading -> {
            UploadStateConfig(Color(0xFFE0E0E0), Color.Gray, IconBlue, "Enviando...")
        }
        UploadState.Success -> {
            UploadStateConfig(Color(0xFFC8E6C9), Color.Green, Color.Green, "Enviado com sucesso!")
        }
        UploadState.Error -> {
            UploadStateConfig(Color(0xFFFFCDD2), Color.Red, Color.Red, "Erro ao enviar. Tente novamente.")
        }
        else -> {
            UploadStateConfig(
                Color.Transparent,
                if (fileName != null) Color.Black else TextGray,
                IconBlue,
                fileName ?: "Clique para selecionar o arquivo PDF"
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .drawBehind {
                drawRoundRect(
                    color = if (uploadState == UploadState.Idle) Color(0xFFD1D5DB) else config.backgroundColor,
                    style = if (uploadState == UploadState.Idle) stroke else Stroke(width = 0f),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
            }
            .background(if (uploadState != UploadState.Idle) config.backgroundColor else Color.Transparent)
            .clickable(enabled = uploadState != UploadState.Uploading) { onUploadClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (uploadState) {
                    UploadState.Uploading -> Icons.Default.Check
                    UploadState.Success -> Icons.Default.Check
                    UploadState.Error -> Icons.Default.Check
                    else -> Icons.Outlined.FileUpload
                },
                contentDescription = null,
                tint = config.iconTint
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = config.contentText,
                color = config.textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfile() {
    ProfileScreen()
}
