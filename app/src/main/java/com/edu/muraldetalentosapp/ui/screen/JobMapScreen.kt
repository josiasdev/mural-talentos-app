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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.edu.muraldetalentosapp.ui.components.JobCard
import com.edu.muraldetalentosapp.data.model.JobPosting
import com.edu.muraldetalentosapp.ui.theme.BluePrimary
import com.edu.muraldetalentosapp.viewmodel.JobsViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobMapScreen(
    onNavigateBack: () -> Unit,
    viewModel: JobsViewModel
) {
    val jobs by viewModel.jobs.collectAsState()

    val context = LocalContext.current

    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

    var selectedJob by remember { mutableStateOf<JobPosting?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(-4.9685, -39.0150)) // Centro de Quixadá (ajustado)
        }
    }

    LaunchedEffect(jobs) {
        mapView.overlays.clear()

        jobs.forEach { job ->
            if (job.latitude != null && job.longitude != null) {
                val marker = Marker(mapView)
                marker.position = GeoPoint(job.latitude, job.longitude)
                marker.title = job.title
                marker.snippet = job.company
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                marker.setOnMarkerClickListener { _, _ ->
                    selectedJob = job
                    true
                }
                mapView.overlays.add(marker)
            }
        }
        mapView.invalidate()
    }

    Scaffold(
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { mapView }
            )
        }

        if (selectedJob != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedJob = null },
                sheetState = sheetState,
                containerColor = Color.White
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
