package com.fkurevija.bioshot.feature.symptom.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.SymptomEntryEntity
import com.fkurevija.bioshot.ui.theme.BioShotTheme
import java.text.SimpleDateFormat
import java.util.*

enum class TimelineType { INJECTION, SYMPTOM }

data class TimelineItem(
    val timestamp: Long,
    val type: TimelineType,
    val title: String,
    val details: String,
    val id: Long
)

/** Card displaying a timeline entry (injection or symptom). */
@Composable
fun TimelineCard(
    item: TimelineItem,
    onDelete: (() -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", androidx.compose.ui.platform.LocalLocale.current.platformLocale).apply {
        timeZone = TimeZone.getDefault()
    }
    val formattedDate = dateFormat.format(Date(item.timestamp))

    val containerColor = when (item.type) {
        TimelineType.INJECTION -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        TimelineType.SYMPTOM -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (item.type == TimelineType.INJECTION) Icons.Rounded.Medication else Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = if (item.type == TimelineType.INJECTION) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = if (item.type == TimelineType.INJECTION) "Injection" else "Symptom",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Text(
                text = item.details,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
