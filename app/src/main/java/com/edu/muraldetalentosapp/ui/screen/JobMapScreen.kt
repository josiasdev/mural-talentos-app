package com.edu.muraldetalentosapp.ui.screen

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.edu.muraldetalentosapp.ui.components.JobCard
import com.edu.muraldetalentosapp.data.model.JobPosting
import com.edu.muraldetalentosapp.ui.theme.BluePrimary
import com.edu.muraldetalentosapp.viewmodel.JobsViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobMapScreen(
    onNavigateBack: () -> Unit,
    viewModel: JobsViewModel
) {
    val jobs by viewModel.jobs.collectAsState()

    var selectedJob by remember { mutableStateOf<JobPosting?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val quixada = LatLng(-4.9685, -39.0150)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(quixada, 15f)
    }

    val context = LocalContext.current
    var isLocationEnabled by remember { mutableStateOf(false) }
    
    val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        isLocationEnabled = granted
        if (granted) {
             try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                         cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                             LatLng(location.latitude, location.longitude), 15f
                         )
                    }
                }
             } catch (e: SecurityException) {
             }
        }
    }
    

    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val fineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (fineLocation == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            coarseLocation == android.content.pm.PackageManager.PERMISSION_GRANTED) {
             isLocationEnabled = true
             try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                         cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                             LatLng(location.latitude, location.longitude), 15f
                         )
                    }
                }
             } catch (e: SecurityException) {
             }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Vagas Próximas", color = BluePrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = BluePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = com.google.maps.android.compose.MapProperties(isMyLocationEnabled = isLocationEnabled),
                uiSettings = com.google.maps.android.compose.MapUiSettings(myLocationButtonEnabled = true)
            ) {
                jobs.forEach { job ->
                    if (job.latitude != null && job.longitude != null) {
                        Marker(
                            state = MarkerState(position = LatLng(job.latitude, job.longitude)),
                            title = job.title,
                            snippet = job.company,
                            onClick = {
                                selectedJob = job
                                false 
                            }
                        )
                    }
                }
            }
        }

        if (selectedJob != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedJob = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Box(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                    JobCard(
                        job = selectedJob!!,
                        onClick = {
                            viewModel.toggleApplication(selectedJob!!.id)
                            selectedJob = null
                        }
                    )
                }
            }
        }
    }
}
