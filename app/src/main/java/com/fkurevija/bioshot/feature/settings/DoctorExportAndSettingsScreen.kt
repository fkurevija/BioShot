package com.fkurevija.bioshot.feature.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.fkurevija.bioshot.core.database.BioShotDatabase
import com.fkurevija.bioshot.core.database.DatabaseBackupManager
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.database.SymptomEntryEntity
import com.fkurevija.bioshot.core.database.WeightLogEntity
import com.fkurevija.bioshot.core.notifications.NotificationScheduler
import com.fkurevija.bioshot.core.schedule.ScheduleEngine
import com.fkurevija.bioshot.feature.settings.components.ConflictStrategy
import com.fkurevija.bioshot.feature.settings.components.DoctorReportSection
import com.fkurevija.bioshot.feature.settings.components.ImportDataDialog
import com.fkurevija.bioshot.ui.theme.BioShotTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorExportAndSettingsScreen(
    medications: List<MedicationEntity>,
    injectionLogs: List<InjectionLogEntity>,
    symptomEntries: List<SymptomEntryEntity>,
    forceDark: Boolean = false,
    remindersEnabled: Boolean = false,
    reminderHour: Int = -1,
    reminderMinute: Int = -1,
    migrationError: String? = null,
    onForceDarkChanged: (Boolean) -> Unit = {},
    onRemindersChanged: (Boolean) -> Unit = {},
    onReminderTimeChanged: (Int, Int) -> Unit = { _, _ -> },
    onCheckDatabaseErrors: () -> Unit = {},
    onResetDatabase: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingBackupFile by remember { mutableStateOf<File?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && pendingBackupFile != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    pendingBackupFile?.inputStream()?.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                android.widget.Toast.makeText(context, "Backup saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Failed to save backup file", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        pendingBackupFile = null
    }

    if (showImportDialog) {
        ImportDataDialog(
            onDismiss = { showImportDialog = false },
            onImportConfirmed = { uri, conflictStrategy ->
                scope.launch {
                    try {
                        val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        if (!jsonString.isNullOrBlank()) {
                            val jsonObject = Json.parseToJsonElement(jsonString).jsonObject
                            val tables = jsonObject["tables"]?.jsonObject

                            val db = BioShotDatabase.getDatabase(context)

                            if (conflictStrategy == ConflictStrategy.OVERWRITE) {
                                db.medicationDao().getAllMedicationsList().forEach { db.medicationDao().deleteMedication(it) }
                                db.injectionLogDao().getAllInjectionLogsList().forEach { db.injectionLogDao().deleteInjectionLog(it) }
                                db.symptomDao().getAllSymptomEntriesList().forEach { db.symptomDao().deleteSymptomEntry(it) }
                                db.weightDao().getAllWeightLogsList().forEach { db.weightDao().deleteWeightLog(it) }
                            }

                            tables?.get("medications")?.jsonArray?.forEach { element ->
                                val med = Json.decodeFromJsonElement<MedicationEntity>(element)
                                when (conflictStrategy) {
                                    ConflictStrategy.MERGE -> db.medicationDao().insertMedication(med.copy(id = 0L))
                                    ConflictStrategy.OVERWRITE -> db.medicationDao().insertMedication(med)
                                    ConflictStrategy.SKIP -> {
                                        if (db.medicationDao().getMedicationById(med.id) == null) {
                                            db.medicationDao().insertMedication(med)
                                        }
                                    }
                                }
                            }
                            tables?.get("injection_logs")?.jsonArray?.forEach { element ->
                                val log = Json.decodeFromJsonElement<InjectionLogEntity>(element)
                                when (conflictStrategy) {
                                    ConflictStrategy.MERGE -> db.injectionLogDao().insertInjectionLog(log.copy(id = 0L))
                                    ConflictStrategy.OVERWRITE -> db.injectionLogDao().insertInjectionLog(log)
                                    ConflictStrategy.SKIP -> {
                                        if (db.injectionLogDao().getInjectionLogById(log.id) == null) {
                                            db.injectionLogDao().insertInjectionLog(log)
                                        }
                                    }
                                }
                            }
                            tables?.get("symptom_entries")?.jsonArray?.forEach { element ->
                                val symptom = Json.decodeFromJsonElement<SymptomEntryEntity>(element)
                                when (conflictStrategy) {
                                    ConflictStrategy.MERGE -> db.symptomDao().insertSymptomEntry(symptom.copy(id = 0L))
                                    ConflictStrategy.OVERWRITE -> db.symptomDao().insertSymptomEntry(symptom)
                                    ConflictStrategy.SKIP -> {
                                        if (db.symptomDao().getSymptomEntryById(symptom.id) == null) {
                                            db.symptomDao().insertSymptomEntry(symptom)
                                        }
                                    }
                                }
                            }
                            tables?.get("weight_logs")?.jsonArray?.forEach { element ->
                                val weight = Json.decodeFromJsonElement<WeightLogEntity>(element)
                                when (conflictStrategy) {
                                    ConflictStrategy.MERGE -> db.weightDao().insertWeightLog(weight.copy(id = 0L))
                                    ConflictStrategy.OVERWRITE -> db.weightDao().insertWeightLog(weight)
                                    ConflictStrategy.SKIP -> {
                                        if (db.weightDao().getWeightLogById(weight.id) == null) {
                                            db.weightDao().insertWeightLog(weight)
                                        }
                                    }
                                }
                            }

                            android.widget.Toast.makeText(context, "Data imported successfully!", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        android.widget.Toast.makeText(context, "Import failed: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (showExportDialog && pendingBackupFile != null) {
        AlertDialog(
            onDismissRequest = {
                showExportDialog = false
                pendingBackupFile = null
            },
            title = { Text("Export Data Backup") },
            text = { Text("Choose how you would like to export your BioShot data backup file.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        val backupFile = pendingBackupFile
                        if (backupFile != null) {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                backupFile
                            )
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_SUBJECT, "BioShot Data Backup")
                                putExtra(Intent.EXTRA_STREAM, uri)
                                type = "application/json"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Export Data Backup").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(shareIntent)
                        }
                        pendingBackupFile = null
                    }
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        val defaultName = "BioShot_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
                        saveFileLauncher.launch(defaultName)
                    }
                ) {
                    Text("Save to Device")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Doctor Visit Export & Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
            // Report Export Section
            DoctorReportSection(
                medications = medications,
                injectionLogs = injectionLogs,
                symptomEntries = symptomEntries
            )

            // Settings Section
            Text(
                text = "App Settings & Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Column {
                                Text(text = "Injection Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                val timeText = if (reminderHour >= 0 && reminderMinute >= 0) {
                                    String.format(androidx.compose.ui.platform.LocalLocale.current.platformLocale, "Preferred time: %02d:%02d", reminderHour, reminderMinute)
                                } else {
                                    "Default time: Derived from last dose"
                                }
                                Text(text = timeText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = { enabled ->
                                onRemindersChanged(enabled)
                                if (enabled) {
                                    val med = medications.firstOrNull()
                                    val log = injectionLogs.firstOrNull()
                                    if (med != null) {
                                        val lastTime = log?.timestamp ?: med.createdAt
                                        val nextDose = ScheduleEngine.calculateNextInjection(
                                            lastInjectionDate = lastTime,
                                            scheduleType = med.frequency,
                                            customIntervalDays = med.customIntervalDays
                                        ).nextTimestamp
                                        NotificationScheduler.scheduleReminder(
                                            context = context,
                                            nextDoseTimeMs = nextDose,
                                            medicationName = med.name,
                                            dosage = med.dosage,
                                            preferredHour = reminderHour,
                                            preferredMinute = reminderMinute
                                        )
                                    }
                                } else {
                                    NotificationScheduler.cancelReminder(context)
                                }
                            }
                        )
                    }

                    if (remindersEnabled) {
                        var showTimePicker by remember { mutableStateOf(false) }
                        if (showTimePicker) {
                            val timePickerState = rememberTimePickerState(
                                initialHour = if (reminderHour >= 0) reminderHour else 9,
                                initialMinute = if (reminderMinute >= 0) reminderMinute else 0,
                                is24Hour = false
                            )
                            AlertDialog(
                                onDismissRequest = { showTimePicker = false },
                                title = { Text("Configure Reminder Time") },
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
                                            onReminderTimeChanged(timePickerState.hour, timePickerState.minute)
                                            showTimePicker = false
                                        }
                                    ) {
                                        Text("Save Time")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                                }
                            )
                        }

                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Set Custom Reminder Time of Day")
                        }
                    }

                    HorizontalDivider()

                    // 2. Dark Theme
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Column {
                                Text(text = "Dark Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "Follow system theme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = forceDark, onCheckedChange = onForceDarkChanged)
                    }

                    HorizontalDivider()

                    // 3. Database Errors Check / Reset
                    var hasChecked by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = if (hasChecked && migrationError == null) {
                                Modifier.weight(1f)
                            } else {
                                Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (hasChecked) {
                                            hasChecked = false
                                        } else {
                                            hasChecked = true
                                            onCheckDatabaseErrors()
                                        }
                                    }
                            },
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when {
                                !hasChecked -> {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Column {
                                        Text(
                                            text = "Check for Database Errors",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tap to verify database integrity and check for migration issues.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                migrationError != null -> {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Column {
                                        Text(
                                            text = "Reset Database",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = migrationError
                                                ?: "Database migration failed. Tap Reset to erase all data.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Column {
                                        Text(
                                            text = "No Database Errors Found",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Your database is healthy.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                           )
                                    }
                                }
                            }
                        }
                        if (hasChecked && migrationError != null) {
                            Button(
                                onClick = { onResetDatabase() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text("Reset Now")
                            }
                        }
                    }

                    HorizontalDivider()

                    // 4. Backup Your Data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    val backupFile = DatabaseBackupManager.createManualBackup(context)
                                    if (backupFile != null) {
                                        pendingBackupFile = backupFile
                                        showExportDialog = true
                                    } else {
                                        android.widget.Toast.makeText(context, "No data to backup yet", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text(
                                    text = "Backup Your Data",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Save a secure copy of your treatment logs, weight, and history.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider()

                    // 5. Import My Data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showImportDialog = true
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Restore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text(
                                    text = "Import My Data",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Restore your treatment logs and history from a backup file.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider()

                    // 6. About BioShot (at the bottom)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Column {
                                Text(text = "About BioShot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "Version 1.0.0 • Biologic & Biosimilar Tracker", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DoctorExportAndSettingsScreenPreview() {
    BioShotTheme {
        DoctorExportAndSettingsScreen(
            medications = emptyList(),
            injectionLogs = emptyList(),
            symptomEntries = emptyList()
        )
    }
}
