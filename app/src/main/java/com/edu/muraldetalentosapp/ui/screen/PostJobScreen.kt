package com.edu.muraldetalentosapp.ui.screen

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edu.muraldetalentosapp.ui.theme.BluePrimary
import com.edu.muraldetalentosapp.viewmodel.JobsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobScreen(
    viewModel: JobsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(state.isPostedSuccess) {
        if (state.isPostedSuccess) {
            Toast.makeText(context, "Vaga publicada com sucesso!", Toast.LENGTH_SHORT).show()
            viewModel.resetSuccessMessage()
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Nova Vaga",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Text(
                            "Preencha os detalhes da vaga",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, scrolledContainerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = { viewModel.publishJob() },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publicar Vaga")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "* Campos obrigatórios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            SectionCard(
                title = "Informações da Vaga",
                subtitle = "Dados principais da oportunidade",
                icon = Icons.Outlined.BusinessCenter
            ) {
                CustomTextField(
                    value = state.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = "Título da Vaga *",
                    placeholder = "Ex: Vendedor Interno",
                    isError = state.titleError
                )
                CustomTextField(
                    value = state.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = "Descrição *",
                    placeholder = "Descreva as principais atividades e requisitos...",
                    singleLine = false,
                    modifier = Modifier.height(120.dp),
                    isError = state.descriptionError
                )
                CustomTextField(
                    value = state.location,
                    onValueChange = { viewModel.onLocationChange(it) },
                    label = "Localização *",
                    placeholder = "Ex: São Paulo, SP",
                    isError = state.locationError
                )
                CustomTextField(
                    value = state.contractType,
                    onValueChange = { viewModel.onContractTypeChange(it) },
                    label = "Tipo de Contrato *",
                    placeholder = "Ex: CLT, PJ, Estágio",
                    isError = state.contractError
                )
            }
            
            SectionCard(
                title = "Localização no Mapa",
                subtitle = "Toque no mapa para definir o local exato",
                icon = Icons.Outlined.BusinessCenter
            ) {
                 val quixada = com.google.android.gms.maps.model.LatLng(-4.9685, -39.0150)
                 var markerPosition by remember { 
                     mutableStateOf(
                         if (state.latitude != null && state.longitude != null) 
                             com.google.android.gms.maps.model.LatLng(state.latitude!!, state.longitude!!) 
                         else null
                     ) 
                 }
                 
                 val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
                     position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(quixada, 13f)
                 }

                 Box(
                     modifier = Modifier
                         .fillMaxWidth()
                         .height(300.dp)
                         .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                 ) {
                     com.google.maps.android.compose.GoogleMap(
                         modifier = Modifier.fillMaxSize(),
                         cameraPositionState = cameraPositionState,
                         onMapClick = { latLng ->
                             markerPosition = latLng
                             viewModel.onLatLongChange(latLng.latitude, latLng.longitude)
                             
                             try {
                                 val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                     geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                                         if (addresses.isNotEmpty()) {
                                             val address = addresses[0]
                                             val addressText = address.getAddressLine(0) ?: ""
                                             viewModel.onLocationChange(addressText)
                                         }
                                     }
                                 } else {
                                     @Suppress("DEPRECATION")
                                     val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                                     if (!addresses.isNullOrEmpty()) {
                                         val address = addresses[0]
                                         val addressText = address.getAddressLine(0) ?: ""
                                         viewModel.onLocationChange(addressText)
                                     }
                                 }
                             } catch (e: Exception) {
                                 // Ignore errors
                             }
                         }
                     ) {
                         if (markerPosition != null) {
                             com.google.maps.android.compose.Marker(
                                 state = com.google.maps.android.compose.MarkerState(position = markerPosition!!)
                             )
                         }
                     }
                 }
            }

            SectionCard(
                title = "Remuneração",
                subtitle = "Defina a faixa salarial da vaga"
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Checkbox(
                        checked = state.isSalaryNegotiable,
                        onCheckedChange = { viewModel.onSalaryNegotiableChange(it) },
                        colors = CheckboxDefaults.colors(checkedColor = BluePrimary)
                    )
                    Text("Salário a combinar", color = MaterialTheme.colorScheme.onSurface)
                }

                if (!state.isSalaryNegotiable) {
                    CustomTextField(
                        value = state.salary,
                        onValueChange = { viewModel.onSalaryChange(it) },
                        label = "Faixa Salarial *",
                        placeholder = "Ex: R$ 1.800 - R$ 2.500",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = state.salaryError
                    )
                }
            }

            SectionCard(
                title = "Imagem da Vaga",
                subtitle = "Adicione uma imagem para destacar a vaga (opcional)",
                icon = Icons.Outlined.Image
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { viewModel.onImageUrlChange("imagem_mockada.jpg") }, // Simula upload
                    contentAlignment = Alignment.Center
                ) {
                    if (state.imageUrl.isNotBlank()) {
                        Text("Imagem selecionada!", color = BluePrimary)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.UploadFile, contentDescription = null, tint = BluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clique para adicionar imagem", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Text(
                    text = "Tamanho máximo: 5MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Composable
fun SectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) // Borda cinza suave
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF2563EB), // Azul
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            content()
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    isError: Boolean = false
) {
    Column(modifier = modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        val containerColor = MaterialTheme.colorScheme.surfaceVariant


        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                disabledContainerColor = containerColor,
                errorContainerColor = MaterialTheme.colorScheme.errorContainer,
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions
        )
        if (isError) {
            Text(
                text = "Campo obrigatório",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}