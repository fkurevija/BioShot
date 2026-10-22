package com.fkurevija.bioshot.feature.log

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.core.model.ScheduleType
import com.fkurevija.bioshot.ui.theme.BioShotTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjectionLogScreen(
    medications: List<MedicationEntity>,
    injectionLogs: List<InjectionLogEntity>,
    onSaveInjectionLog: (Long, InjectionSite, String, String?, String?, Int?, List<String>) -> Unit,
    onDeleteInjectionLog: (InjectionLogEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSite by remember { mutableStateOf(InjectionSite.ABDOMEN_LEFT) }
    var selectedMedicationId by remember { mutableStateOf(medications.firstOrNull()?.id ?: 0L) }
    var dosageGiven by remember { mutableStateOf(medications.firstOrNull()?.dosage ?: "40mg") }
    var lotNumber by remember { mutableStateOf("") }
    var painLevel by remember { mutableStateOf(1f) }
    var notes by remember { mutableStateOf("") }

    // Update default dosage when medication changes
    LaunchedEffect(selectedMedicationId, medications) {
        val med = medications.find { it.id == selectedMedicationId }
        if (med != null) {
            dosageGiven = med.dosage
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Injection Log & Body Map") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Site Rotation & History",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select an injection site on the interactive body map below and record your injection history to ensure proper rotation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Interactive Body Map Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Interactive Body Map",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Visual Body Map Selector Grid
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Current Selected: ${selectedSite.name.replace("_", " ")}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Site selector chips in rows
                            val sites = InjectionSite.entries
                            val rows = sites.chunked(2)
                            rows.forEach { rowSites ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowSites.forEach { site ->
                                        val isSelected = site == selectedSite
                                        // Check recency from logs
                                        val lastUsedLog = injectionLogs.find { it.site == site }
                                        val recentBadge = lastUsedLog != null

                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedSite = site },
                                            label = { Text(site.name.replace("_", " ")) },
                                            leadingIcon = {
                                                if (recentBadge) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.History,
                                                        contentDescription = "Recently Used",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Rotation Hint
                    val lastLogForSite = injectionLogs.find { it.site == selectedSite }
                    if (lastLogForSite != null) {
                        val daysAgo = (System.currentTimeMillis() - lastLogForSite.timestamp) / (1000 * 60 * 60 * 24)
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Warning: This site was used $daysAgo days ago. Ensure adequate rotation spacing.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Great choice! No recent injections recorded in this site.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // New Injection Form Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Record New Injection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = dosageGiven,
                        onValueChange = { dosageGiven = it },
                        label = { Text("Dosage Administered") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = lotNumber,
                        onValueChange = { lotNumber = it },
                        label = { Text("Lot Number (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Column {
                        Text(
                            text = "Pain Level: ${painLevel.toInt()} / 10",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = painLevel,
                            onValueChange = { painLevel = it },
                            valueRange = 0f..10f,
                            steps = 9
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Side Effects") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Button(
                        onClick = {
                            if (medications.isNotEmpty()) {
                                val medId = if (selectedMedicationId != 0L) selectedMedicationId else medications.first().id
                                onSaveInjectionLog(
                                    medId,
                                    selectedSite,
                                    dosageGiven,
                                    lotNumber.ifBlank { null },
                                    notes.ifBlank { null },
                                    painLevel.toInt(),
                                    emptyList()
                                )
                                lotNumber = ""
                                notes = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = medications.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Injection Log")
                    }
                }
            }

            // Past Logs History Section
            Text(
                text = "Injection History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (injectionLogs.isEmpty()) {
                Text(
                    text = "No injection history recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                injectionLogs.forEach { log ->
                    InjectionLogItemCard(
                        log = log,
                        onDelete = { onDeleteInjectionLog(log) }
                    )
                }
            }
        }
    }
}

@Composable
fun InjectionLogItemCard(
    log: InjectionLogEntity,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", androidx.compose.ui.platform.LocalLocale.current.platformLocale)
    val formattedDate = dateFormat.format(Date(log.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
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
                Text(
                    text = log.site.name.replace("_", " "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete Log",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = "Dosage: ${log.dosageGiven} | Pain: ${log.painLevel ?: 0}/10",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Date: $formattedDate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!log.lotNumber.isNullOrBlank()) {
                Text(
                    text = "Lot #: ${log.lotNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (!log.notes.isNullOrBlank()) {
                Text(
                    text = "Notes: ${log.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InjectionLogScreenPreview() {
    BioShotTheme {
        InjectionLogScreen(
            medications = listOf(
                MedicationEntity(
                    id = 1,
                    name = "Humira",
                    dosage = "40mg",
                    frequency = ScheduleType.BIWEEKLY,
                    deviceType = DeviceType.PEN
                )
            ),
            injectionLogs = listOf(
                InjectionLogEntity(
                    id = 1,
                    medicationId = 1,
                    timestamp = System.currentTimeMillis(),
                    site = InjectionSite.ABDOMEN_LEFT,
                    dosageGiven = "40mg",
                    painLevel = 2,
                    notes = "Easy injection"
                )
            ),
            onSaveInjectionLog = { _, _, _, _, _, _, _ -> },
            onDeleteInjectionLog = {}
        )
    }
}
