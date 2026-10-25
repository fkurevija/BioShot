package com.fkurevija.bioshot.feature.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.core.model.ScheduleType
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.feature.log.components.InjectionLogBodyMap
import com.fkurevija.bioshot.feature.log.components.InjectionLogInjectionForm
import com.fkurevija.bioshot.feature.log.components.InjectionLogItemCard
import com.fkurevija.bioshot.ui.theme.BioShotTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjectionLogScreen(
    medications: List<MedicationEntity>,
    injectionLogs: List<InjectionLogEntity>,
    onSaveInjectionLog: (Long, InjectionSite, String, String?, String?, Int?, List<String>, Long) -> Unit,
    onUpdateInjectionLog: (InjectionLogEntity) -> Unit,
    onDeleteInjectionLog: (InjectionLogEntity) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSite by remember { mutableStateOf(InjectionSite.ABDOMEN_LEFT) }
    var selectedMedicationId by remember { mutableStateOf(medications.firstOrNull()?.id ?: 0L) }
    var dosageGiven by remember { mutableStateOf(medications.firstOrNull()?.dosage ?: "40mg") }
    var lotNumber by remember { mutableStateOf("") }
    var painLevel by remember { mutableStateOf(1f) }
    var notes by remember { mutableStateOf("") }
    var selectedTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var logToEdit by remember { mutableStateOf<InjectionLogEntity?>(null) }

    val recentSites = remember(injectionLogs) {
        injectionLogs.take(3).map { it.site }
    }
    val allSites = remember { InjectionSite.entries }
    val recommendedSite = remember(recentSites) {
        allSites.firstOrNull { !recentSites.contains(it) } ?: allSites.first()
    }

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
                                timeInMillis = tempDateMillis
                                set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                                set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                            }
                            tempDateMillis = localCal.timeInMillis
                        }
                        showDatePicker = false
                        showTimePicker = true
                    }
                ) {
                    Text("Next (Time)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = selectedTimestamp }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Injection Time") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
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
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
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
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InjectionLogBodyMap(
                        selectedSite = selectedSite,
                        onSiteSelected = { selectedSite = it }
                    )
                }
            }

            // Injection Form Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (medications.isEmpty()) {
                        Text(
                            text = "Please add a medication in the Medications tab before recording an injection.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        InjectionLogInjectionForm(
                            medications = medications,
                            selectedMedicationId = selectedMedicationId,
                            onMedicationSelected = { selectedMedicationId = it },
                            dosageGiven = dosageGiven,
                            onDosageChanged = { dosageGiven = it },
                            lotNumber = lotNumber,
                            onLotNumberChanged = { lotNumber = it },
                            painLevel = painLevel,
                            onPainLevelChanged = { painLevel = it },
                            notes = notes,
                            onNotesChanged = { notes = it },
                            selectedSite = selectedSite,
                            onSiteSelected = { selectedSite = it },
                            recentSites = recentSites,
                            recommendedSite = recommendedSite,
                            selectedTimestamp = selectedTimestamp,
                            onShowDateTimePicker = { showDatePicker = true },
                            onSave = {
                                if (medications.isNotEmpty()) {
                                    val medId = if (selectedMedicationId != 0L) selectedMedicationId else medications.first().id
                                    onSaveInjectionLog(
                                        medId,
                                        selectedSite,
                                        dosageGiven,
                                        lotNumber.ifBlank { null },
                                        notes.ifBlank { null },
                                        painLevel.toInt(),
                                        emptyList(),
                                        selectedTimestamp
                                    )
                                    lotNumber = ""
                                    notes = ""
                                }
                            }
                        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInjectionLogDialog(
    log: InjectionLogEntity,
    medications: List<MedicationEntity>,
    onDismiss: () -> Unit,
    onSave: (InjectionLogEntity) -> Unit
) {
    var dosage by remember { mutableStateOf(log.dosageGiven) }
    var lotNumber by remember { mutableStateOf(log.lotNumber ?: "") }
    var painLevel by remember { mutableStateOf((log.painLevel ?: 1).toFloat()) }
    var notes by remember { mutableStateOf(log.notes ?: "") }
    var selectedSite by remember { mutableStateOf(log.site) }
    var selectedTimestamp by remember { mutableStateOf(log.timestamp) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempDateMillis by remember { mutableStateOf(log.timestamp) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", androidx.compose.ui.platform.LocalLocale.current.platformLocale).apply {
        timeZone = TimeZone.getDefault()
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedTimestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = millis
                        }
                        val localCal = Calendar.getInstance(TimeZone.getDefault()).apply {
                            timeInMillis = tempDateMillis
                            set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                            set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                        }
                        tempDateMillis = localCal.timeInMillis
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next (Time)") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = selectedTimestamp }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Injection Time") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
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
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Injection Log") },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Date & Time:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dateFormat.format(Date(selectedTimestamp)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lotNumber,
                    onValueChange = { lotNumber = it },
                    label = { Text("Lot #") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = painLevel,
                        onValueChange = { painLevel = it },
                        valueRange = 0f..10f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Pain: ${painLevel.toInt()}/10",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        log.copy(
                            dosageGiven = dosage,
                            lotNumber = lotNumber.ifBlank { null },
                            notes = notes.ifBlank { null },
                            painLevel = painLevel.toInt(),
                            site = selectedSite,
                            timestamp = selectedTimestamp
                        )
                    )
                }
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

@Composable
@Preview(showBackground = true)
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
            onSaveInjectionLog = { _, _, _, _, _, _, _, _ -> },
            onUpdateInjectionLog = {},
            onDeleteInjectionLog = {},
            onNavigateToSettings = {}
        )
    }
}
