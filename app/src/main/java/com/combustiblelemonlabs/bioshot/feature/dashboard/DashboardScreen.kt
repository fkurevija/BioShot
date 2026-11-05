package com.combustiblelemonlabs.bioshot.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combustiblelemonlabs.bioshot.core.database.InjectionLogEntity
import com.combustiblelemonlabs.bioshot.core.database.MedicationEntity
import com.combustiblelemonlabs.bioshot.core.database.WeightLogEntity
import com.combustiblelemonlabs.bioshot.core.model.DeviceType
import com.combustiblelemonlabs.bioshot.core.model.InjectionSite
import com.combustiblelemonlabs.bioshot.core.model.ScheduleType
import com.combustiblelemonlabs.bioshot.core.schedule.ScheduleEngine
import com.combustiblelemonlabs.bioshot.feature.dashboard.components.NextDoseCountdownCard
import com.combustiblelemonlabs.bioshot.feature.dashboard.components.LatestInjectionInfoCard
import com.combustiblelemonlabs.bioshot.feature.dashboard.components.BiologicHealthTrackerCard
import com.combustiblelemonlabs.bioshot.ui.theme.BioShotTheme
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    medications: List<MedicationEntity>,
    injectionLogs: List<InjectionLogEntity>,
    weightLogs: List<WeightLogEntity>,
    onQuickLogClick: () -> Unit,
    onNavigateToMedications: () -> Unit,
    onNavigateToLog: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAddWeightLog: (Double, String, Double?, Double?, String?) -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val scrollState = rememberScrollState()
    val latestMedication = medications.firstOrNull()
    val latestLog = injectionLogs.firstOrNull()

    var showAddWeightDialog by remember { mutableStateOf(false) }

    if (showAddWeightDialog) {
        AddWeightDialog(
            onDismiss = { showAddWeightDialog = false },
            onSave = { w, unit, fat, waist, notes ->
                onAddWeightLog(w, unit, fat, waist, notes)
                showAddWeightDialog = false
            }
        )
    }

    // Next dose calculation
    val nextDoseTime = remember(latestMedication, latestLog) {
        if (latestMedication == null) return@remember null
        val lastTime = latestLog?.timestamp ?: latestMedication.createdAt
        val res = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = lastTime,
            scheduleType = latestMedication.frequency,
            customIntervalDays = latestMedication.customIntervalDays
        )
        res.nextTimestamp
    }

    val now = System.currentTimeMillis()
    val diffMillis = if (nextDoseTime != null) nextDoseTime - now else 0L

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BioShot Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Track your biologic therapy & wellness",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalIconButton(onClick = onNavigateToMedications) {
                    Icon(
                        imageVector = Icons.Rounded.Medication,
                        contentDescription = "Manage Medications"
                    )
                }
                FilledTonalIconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings"
                    )
                }
            }

            // Next Dose Countdown Card
            NextDoseCountdownCard(
                latestMedication = latestMedication,
                latestLog = latestLog,
                diffMillis = diffMillis,
                onNavigateToMedications = onNavigateToMedications,
                onQuickLogClick = onQuickLogClick
            )

            // Biologic Health & Weight Tracker Card
            BiologicHealthTrackerCard(
                weightLogs = weightLogs,
                onAddWeightClick = { showAddWeightDialog = true }
            )

            // Last Injection Site Reminder Card
            LatestInjectionInfoCard(
                latestLog = latestLog,
                onNavigateToLog = onNavigateToLog
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onSave: (Double, String, Double?, Double?, String?) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("lbs") }
    var bodyFatText by remember { mutableStateOf("") }
    var waistText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Weight & Health Metric") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight ($unit)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = unit == "lbs",
                        onClick = { unit = "lbs" },
                        label = { Text("lbs") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = unit == "kg",
                        onClick = { unit = "kg" },
                        label = { Text("kg") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = bodyFatText,
                    onValueChange = { bodyFatText = it },
                    label = { Text("Body Fat % (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = waistText,
                    onValueChange = { waistText = it },
                    label = { Text("Waist cm / in (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Symptoms (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val w = weightText.toDoubleOrNull()
                    if (w != null) {
                        onSave(
                            w,
                            unit,
                            bodyFatText.toDoubleOrNull(),
                            waistText.toDoubleOrNull(),
                            notesText.ifBlank { null }
                        )
                        onDismiss()
                    }
                },
                enabled = weightText.toDoubleOrNull() != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    BioShotTheme {
        DashboardScreen(
            medications = listOf(
                MedicationEntity(
                    id = 1,
                    name = "Humira (Adalimumab)",
                    dosage = "40mg",
                    frequency = ScheduleType.BIWEEKLY,
                    deviceType = DeviceType.PEN
                )
            ),
            injectionLogs = listOf(
                InjectionLogEntity(
                    id = 1,
                    medicationId = 1,
                    timestamp = System.currentTimeMillis() - 86400000L * 3,
                    site = InjectionSite.ABDOMEN_LEFT,
                    dosageGiven = "40mg"
                )
            ),
            weightLogs = emptyList(),
            onQuickLogClick = {},
            onNavigateToMedications = {},
            onNavigateToLog = {},
            onNavigateToSettings = {},
            onAddWeightLog = { _, _, _, _, _ -> }
        )
    }
}
