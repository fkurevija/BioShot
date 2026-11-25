package com.combustiblelemonlabs.bioshot.feature.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combustiblelemonlabs.bioshot.core.database.InjectionLogEntity
import com.combustiblelemonlabs.bioshot.core.database.MedicationEntity
import com.combustiblelemonlabs.bioshot.core.model.InjectionSite
import com.combustiblelemonlabs.bioshot.feature.log.components.InjectionLogBodyMap
import com.combustiblelemonlabs.bioshot.feature.log.components.InjectionLogInjectionForm
import com.combustiblelemonlabs.bioshot.feature.log.components.InjectionLogItemCard
import com.combustiblelemonlabs.bioshot.feature.log.components.EditInjectionLogDialog
import com.combustiblelemonlabs.bioshot.ui.theme.BioShotTheme
import java.util.Calendar
import java.util.TimeZone

private fun calculateRecommendedSite(recentLogs: List<InjectionLogEntity>): InjectionSite {
    val allSites = InjectionSite.entries
    val recentSiteSet = recentLogs.asSequence().take(4).map { (_, _, _, site) -> site }.toSet()
    return allSites.firstOrNull { it !in recentSiteSet } ?: allSites.first()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjectionLogScreen(
    medications: List<MedicationEntity>,
    injectionLogs: List<InjectionLogEntity>,
    onSaveInjectionLog: (Long, InjectionSite, String, String?, String?, Int?, List<String>, Long) -> Unit,
    onUpdateInjectionLog: (InjectionLogEntity) -> Unit,
    onDeleteInjectionLog: (InjectionLogEntity) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSite by remember { mutableStateOf(InjectionSite.ABDOMEN_LEFT) }
    var selectedMedicationId by remember { mutableLongStateOf(medications.firstOrNull()?.id ?: 0L) }

    // Keep selectedMedicationId valid if medications list updates
    LaunchedEffect(medications) {
        if ((selectedMedicationId == 0L) && medications.isNotEmpty()) {
            selectedMedicationId = medications.first().id
        }
    }

    val recommendedSite = remember(injectionLogs) {
        calculateRecommendedSite(injectionLogs)
    }

    var dosageInput by remember { mutableStateOf("") }
    var lotNumberInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var painLevelInput by remember { mutableFloatStateOf(1f) }

    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(value = false) }
    var showTimePicker by remember { mutableStateOf(value = false) }
    var tempDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var logToEdit by remember { mutableStateOf<InjectionLogEntity?>(null) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedTimestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = millis
                            }
                            val localCal = Calendar.getInstance(TimeZone.getDefault()).apply {
                                timeInMillis = selectedTimestamp
                                set(Calendar.YEAR, utcCal[Calendar.YEAR])
                                set(Calendar.MONTH, utcCal[Calendar.MONTH])
                                set(Calendar.DAY_OF_MONTH, utcCal[Calendar.DAY_OF_MONTH])
                            }
                            tempDateMillis = localCal.timeInMillis
                        }
                        showDatePicker = false
                        showTimePicker = true
                    },
                ) { Text("Next (Time)") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                ) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = selectedTimestamp }
        val timePickerState = rememberTimePickerState(
            initialHour = cal[Calendar.HOUR_OF_DAY],
            initialMinute = cal[Calendar.MINUTE],
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Injection Time") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
                            timeInMillis = tempDateMillis
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        selectedTimestamp = calendar.timeInMillis
                        showTimePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    if (logToEdit != null) {
        EditInjectionLogDialog(
            log = logToEdit!!,
            medications = medications,
            onDismiss = { logToEdit = null },
            onSave = { updatedLog ->
                onUpdateInjectionLog(updatedLog)
                logToEdit = null
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Injection & Site Rotation") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Interactive Body Map",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select your injection site on the anatomical map below to ensure proper site rotation and prevent tissue damage.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Body Map Component
            InjectionLogBodyMap(
                selectedSite = selectedSite,
                onSiteSelected = { selectedSite = it }
            )

            // Injection Form Card
            InjectionLogInjectionForm(
                medications = medications,
                selectedMedicationId = selectedMedicationId,
                onMedicationSelected = { selectedMedicationId = it },
                dosageGiven = dosageInput,
                onDosageChanged = { dosageInput = it },
                lotNumber = lotNumberInput,
                onLotNumberChanged = { lotNumberInput = it },
                painLevel = painLevelInput,
                onPainLevelChanged = { painLevelInput = it },
                notes = notesInput,
                onNotesChanged = { notesInput = it },
                selectedSite = selectedSite,
                onSiteSelected = { selectedSite = it },
                recentSites = injectionLogs.asSequence().take(3).map { (_, _, _, site) -> site }.toList(),
                recommendedSite = recommendedSite,
                selectedTimestamp = selectedTimestamp,
                onShowDateTimePicker = { showDatePicker = true },
                onSave = {
                    if (selectedMedicationId != 0L) {
                        val med = medications.find { (id) -> id == selectedMedicationId }
                        onSaveInjectionLog(
                            selectedMedicationId,
                            selectedSite,
                            dosageInput.ifBlank { med?.dosage ?: "40mg" },
                            lotNumberInput.ifBlank { null },
                            notesInput.ifBlank { null },
                            painLevelInput.toInt(),
                            emptyList(),
                            selectedTimestamp
                        )
                        dosageInput = ""
                        lotNumberInput = ""
                        notesInput = ""
                        selectedTimestamp = System.currentTimeMillis()
                    }
                }
            )

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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    injectionLogs.forEach { log ->
                        InjectionLogItemCard(
                            log = log,
                            onEdit = { logToEdit = log },
                            onDelete = { onDeleteInjectionLog(log) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InjectionLogScreenPreview() {
    BioShotTheme {
        InjectionLogScreen(
            medications = emptyList(),
            injectionLogs = emptyList(),
            onSaveInjectionLog = { _, _, _, _, _, _, _, _ -> },
            onUpdateInjectionLog = {},
            onDeleteInjectionLog = {},
            onNavigateToSettings = {}
        )
    }
}
