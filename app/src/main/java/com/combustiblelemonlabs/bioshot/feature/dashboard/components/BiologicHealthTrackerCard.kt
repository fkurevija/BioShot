package com.combustiblelemonlabs.bioshot.feature.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.combustiblelemonlabs.bioshot.core.database.WeightLogEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BiologicHealthTrackerCard(
    weightLogs: List<WeightLogEntity>,
    onAddWeightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestWeight = weightLogs.firstOrNull()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MonitorWeight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Weight & Health Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onAddWeightClick) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Weight Log")
                }
            }

            if (latestWeight != null) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", androidx.compose.ui.platform.LocalLocale.current.platformLocale)
                Text(
                    text = "Latest: ${latestWeight.weight} ${latestWeight.unit}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Recorded on " + dateFormat.format(Date(latestWeight.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (latestWeight.bodyFatPercentage != null || latestWeight.waistCm != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        latestWeight.bodyFatPercentage?.let {
                            Text("Body Fat: $it%", style = MaterialTheme.typography.bodyMedium)
                        }
                        latestWeight.waistCm?.let {
                            Text("Waist: $it cm", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                Text(
                    text = "No weight or health metrics recorded yet. Track your wellness alongside biologic therapy.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onAddWeightClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Weight / Metric")
            }
        }
    }
}
