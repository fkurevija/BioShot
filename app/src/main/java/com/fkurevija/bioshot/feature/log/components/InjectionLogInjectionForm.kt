package com.fkurevija.bioshot.feature.log.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.ui.theme.BioShotTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjectionLogInjectionForm(
    medications: List<MedicationEntity>,
    selectedMedicationId: Long,
    onMedicationSelected: (Long) -> Unit,
    dosageGiven: String,
    onDosageChanged: (String) -> Unit,
    lotNumber: String,
    onLotNumberChanged: (String) -> Unit,
    painLevel: Float,
    onPainLevelChanged: (Float) -> Unit,
    notes: String,
    onNotesChanged: (String) -> Unit,
    selectedSite: InjectionSite,
    onSiteSelected: (InjectionSite) -> Unit,
    recentSites: List<InjectionSite>,
    recommendedSite: InjectionSite,
    selectedTimestamp: Long,
    onShowDateTimePicker: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", androidx.compose.ui.platform.LocalLocale.current.platformLocale).apply {
        timeZone = TimeZone.getDefault()
    }
    val formattedDateTime = dateFormat.format(Date(selectedTimestamp))
    val isRecentSite = recentSites.contains(selectedSite)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Record New Injection",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Site Rotation Warning Banner
        if (isRecentSite) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Rounded.Warning, contentDescription = "Warning")
                        Text(
                            text = "Site Rotation Warning",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "You injected at ${selectedSite.name.replace("_", " ")} within your last 3 injections. Proper site rotation prevents tissue damage and ensures consistent absorption.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(
                        onClick = { onSiteSelected(recommendedSite) },
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onErrorContainer),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Switch to Recommended (${recommendedSite.name.replace("_", " ")})")
                    }
                }
            }
        }

        // Date & Time Selector Card
        OutlinedCard(
            onClick = onShowDateTimePicker,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Injection Date & Time",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formattedDateTime,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Change Date & Time",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Medication selector
        if (medications.isNotEmpty()) {
            val selectedMed = medications.find { it.id == selectedMedicationId }
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {}
            ) {
                OutlinedTextField(
                    value = selectedMed?.name ?: "Select medication",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Medication") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = {}
                ) {
                    medications.forEach { med ->
                        DropdownMenuItem(
                            text = { Text(med.name) },
                            onClick = { onMedicationSelected(med.id) }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = dosageGiven,
                onValueChange = { onDosageChanged(it) },
                label = { Text("Dosage (e.g. 40mg)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = lotNumber,
                onValueChange = { onLotNumberChanged(it) },
                label = { Text("Lot #") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Slider(
                value = painLevel,
                onValueChange = { onPainLevelChanged(it) },
                valueRange = 0f..10f,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Pain: ${painLevel.toInt()}/10",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { onNotesChanged(it) },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onSave() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Log Injection")
        }
    }
}
