package com.edu.muraldetalentosapp.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.muraldetalentosapp.ui.model.JobPosting
import com.edu.muraldetalentosapp.ui.theme.BluePrimary
import com.edu.muraldetalentosapp.ui.theme.TextGray
import com.edu.muraldetalentosapp.viewmodel.JobsViewModel

@Composable
fun CompanyDashboard(
    viewModel: JobsViewModel,
    onNavigateToPostJob: () -> Unit
) {
    val myJobs by viewModel.jobs.collectAsState()

    val activeJobsCount = myJobs.size

    val totalCandidates = activeJobsCount * 5

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Vagas\nAtivas",
                    count = activeJobsCount.toString(),
                    icon = Icons.Outlined.WorkOutline,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Candidatos",
                    count = totalCandidates.toString(),
                    icon = Icons.Outlined.Groups,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Button(
                onClick = onNavigateToPostJob,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Publicar Nova Vaga")
            }
        }

        item {
            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(12.dp))
                    Text("Buscar Candidatos", color = Color.Black)
                }
            }
        }

        item {
            Text("Vagas Publicadas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }


        items(myJobs) { job ->
            CompanyJobCard(job = job)
        }
    }
}


@Composable
fun StatCard(title: String, count: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text(title, color = TextGray, fontSize = 14.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = count,
                color = BluePrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BluePrimary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(32.dp)
                    .alpha(0.8f)
            )
        }
    }
}

@Composable
fun CompanyJobCard(job: JobPosting) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(job.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = BluePrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                job.type,
                                color = BluePrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Outlined.Groups, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextGray)
                        Spacer(Modifier.width(4.dp))
                        Text("15 candidatos", fontSize = 12.sp, color = TextGray) // Mockado por enquanto
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                job.description,
                maxLines = 2,
                color = TextGray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(12.dp))

            Text("📍 ${job.location}", fontSize = 13.sp, color = TextGray)
            Text("💲 ${job.salaryRange ?: "A combinar"}", fontSize = 13.sp, color = TextGray)
            Text("🕒 Publicada em ${job.publishedAt ?: "Recentemente"}", fontSize = 13.sp, color = TextGray)

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ver Candidatos", color = Color.Black)
                }

                OutlinedButton(
                    onClick = {  },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Outlined.Cancel, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Fechar Vaga", color = Color.Black)
                }
            }
        }
    }
}