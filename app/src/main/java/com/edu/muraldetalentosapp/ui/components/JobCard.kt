package com.edu.muraldetalentosapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.edu.muraldetalentosapp.data.model.JobPosting

@Composable
fun JobCard(job: JobPosting, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(text = job.title, style = MaterialTheme.typography.titleMedium)
            Text(text = job.company, style = MaterialTheme.typography.bodyMedium)

            if (!job.imageUrl.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = job.imageUrl,
                    contentDescription = "Imagem da vaga",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp) // Altura agradável para destaque
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(8.dp))

            AssistChip(
                onClick = {},
                label = { Text(job.type) }
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(text = job.location)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(text = job.salaryRange.ifBlank { "A combinar" })
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(text = "Publicada em ${job.publishedAt.ifBlank { "Data desconhecida" }}")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                enabled = !job.isApplied
            ) {
                Text(if (job.isApplied) "Candidatado" else "Candidatar-se")
            }
        }
    }
}
