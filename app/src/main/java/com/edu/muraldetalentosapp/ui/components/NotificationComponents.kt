package com.edu.muraldetalentosapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.muraldetalentosapp.data.model.Notification
import com.edu.muraldetalentosapp.ui.theme.BluePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationBell(
    unreadCount: Int,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.size(48.dp).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = if (unreadCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
            contentDescription = "Notificações",
            tint = BluePrimary
        )
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.Red),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListSheet(
    notifications: List<Notification>,
    onDismiss: () -> Unit,
    onMarkAsRead: (Notification) -> Unit,
    onMarkAllAsRead: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 32.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notificações", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = onMarkAllAsRead) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Marcar todas lidas")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma notificação recente.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notifications) { notification ->
                        NotificationItem(notification = notification, onClick = { onMarkAsRead(notification) })
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onClick: () -> Unit) {
    val bgColor = if (notification.isRead) MaterialTheme.colorScheme.surface else BluePrimary.copy(alpha = 0.1f)
    val date = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(notification.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(if (notification.isRead) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(notification.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(Modifier.height(4.dp))
            Text(notification.message, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}