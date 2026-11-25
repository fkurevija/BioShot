package com.combustiblelemonlabs.bioshot.feature.settings

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
import com.combustiblelemonlabs.bioshot.core.database.BioShotDatabase
import com.combustiblelemonlabs.bioshot.core.database.DatabaseBackupManager
import com.combustiblelemonlabs.bioshot.core.database.InjectionLogEntity
import com.combustiblelemonlabs.bioshot.core.database.MedicationEntity
import com.combustiblelemonlabs.bioshot.core.database.SymptomEntryEntity
import com.combustiblelemonlabs.bioshot.core.database.WeightLogEntity
import com.combustiblelemonlabs.bioshot.core.notifications.CustomReminder
import com.combustiblelemonlabs.bioshot.core.notifications.NotificationScheduler
import com.combustiblelemonlabs.bioshot.core.notifications.ReminderUnit
import com.combustiblelemonlabs.bioshot.core.schedule.ScheduleEngine
import com.combustiblelemonlabs.bioshot.feature.settings.components.ConflictStrategy
import com.combustiblelemonlabs.bioshot.feature.settings.components.DoctorReportSection
import com.combustiblelemonlabs.bioshot.feature.settings.components.ImportDataDialog
import com.combustiblelemonlabs.bioshot.ui.theme.BioShotTheme
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
    modifier: Modifier = Modifier,
    forceDark: Boolean = false,
    remindersEnabled: Boolean = false,
    customReminders: List<CustomReminder> = CustomReminder.DEFAULT_REMINDERS,
    reminderHour: Int = -1,
    reminderMinute: Int = -1,
    migrationError: String? = null,
    onForceDarkChanged: (Boolean) -> Unit = {},
    onRemindersChanged: (Boolean) -> Unit = {},
    onCustomRemindersChanged: (List<CustomReminder>) -> Unit = {},
    onReminderTimeChanged: (Int, Int) -> Unit = { _, _ -> },
    onCheckDatabaseErrors: () -> Unit = {},
    onResetDatabase: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingBackupFile by remember { mutableStateOf<File?>(null) }
    var showExportDialog by remember { mutableStateOf(value = false) }
    var showImportDialog by remember { mutableStateOf(value = false) }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if ((uri != null) && (pendingBackupFile != null)) {
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

    if ((showExportDialog) && (pendingBackupFile != null)) {
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
                    // Helper to reschedule notifications when settings change
                    fun triggerReschedule(
                        enabled: Boolean,
                        remindersList: List<CustomReminder>,
                        newHour: Int = reminderHour,
                        newMinute: Int = reminderMinute
                    ) {
                        val med = medications.firstOrNull()
                        val log = injectionLogs.firstOrNull()
                        if ((med != null) && enabled && (remindersList.isNotEmpty())) {
                            val lastTime = log?.timestamp ?: med.createdAt
                            val nextDose = ScheduleEngine.calculateNextInjection(
                                lastInjectionDate = lastTime,
                                scheduleType = med.frequency,
                                customIntervalDays = med.customIntervalDays
                            ).nextTimestamp
                            NotificationScheduler.scheduleReminders(
                                context = context,
                                nextDoseTimeMs = nextDose,
                                medicationName = med.name,
                                dosage = med.dosage,
                                remindersEnabled = true,
                                reminders = remindersList,
                                preferredHour = newHour,
                                preferredMinute = newMinute
                            )
                        } else {
                            NotificationScheduler.cancelReminders(context)
                        }
                    }

                    var showAddReminderDialog by remember { mutableStateOf(value = false) }

                    // 1. Injection Reminders (Google Calendar Style)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Column {
                                    Text(
                                        text = "Injection Reminders",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val timeText = if ((reminderHour >= 0) && (reminderMinute >= 0)) {
                                        String.format(
                                            androidx.compose.ui.platform.LocalLocale.current.platformLocale,
                                            "Preferred daily time: %02d:%02d",
                                            reminderHour,
                                            reminderMinute
                                        )
                                    } else {
                                        "Default time: Derived from dose schedule"
                                    }
                                    Text(
                                        text = timeText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = remindersEnabled,
                                onCheckedChange = { enabled ->
                                    onRemindersChanged(enabled)
                                    triggerReschedule(enabled, customReminders)
                                }
                            )
                        }

                        if (remindersEnabled) {
                            // Reminders List
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (customReminders.isEmpty()) {
                                        Text(
                                            text = "No reminders configured. Tap 'Add notification' below.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        customReminders.forEach { reminder ->
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
                                                        imageVector = Icons.Rounded.NotificationsActive,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = reminder.getFormattedLabel(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        val updatedList = customReminders.filter { (id) -> id != reminder.id }
                                                        onCustomRemindersChanged(updatedList)
                                                        triggerReschedule(remindersEnabled, updatedList)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Close,
                                                        contentDescription = "Remove reminder",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = { showAddReminderDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add notification")
                                    }
                                }
                            }

                            var showTimePicker by remember { mutableStateOf(value = false) }
                            if (showTimePicker) {
                                val timePickerState = rememberTimePickerState(
                                    initialHour = if (reminderHour >= 0) reminderHour else 9,
                                    initialMinute = if (reminderMinute >= 0) reminderMinute else 0,
                                    is24Hour = false
                                )
                                AlertDialog(
                                    onDismissRequest = { showTimePicker = false },
                                    title = { Text("Configure Preferred Time of Day") },
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
                                                triggerReschedule(remindersEnabled, customReminders, timePickerState.hour, timePickerState.minute)
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
                                Text("Set Preferred Time of Day")
                            }
                        }
                    }

                    if (showAddReminderDialog) {
                        var customAmountInput by remember { mutableStateOf("10") }
                        var selectedUnit by remember { mutableStateOf(ReminderUnit.MINUTES) }

                        AlertDialog(
                            onDismissRequest = { showAddReminderDialog = false },
                            title = { Text("Add Reminder") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Quick Presets:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        SuggestionChip(
                                            onClick = {
                                                val newReminder = CustomReminder(amount = 10, unit = ReminderUnit.MINUTES)
                                                val updatedList = customReminders + newReminder
                                                onCustomRemindersChanged(updatedList)
                                                triggerReschedule(remindersEnabled, updatedList)
                                                showAddReminderDialog = false
                                            },
                                            label = { Text("10 mins") }
                                        )
                                        SuggestionChip(
                                            onClick = {
                                                val newReminder = CustomReminder(amount = 1, unit = ReminderUnit.HOURS)
                                                val updatedList = customReminders + newReminder
                                                onCustomRemindersChanged(updatedList)
                                                triggerReschedule(remindersEnabled, updatedList)
                                                showAddReminderDialog = false
                                            },
                                            label = { Text("1 hour") }
                                        )
                                        SuggestionChip(
                                            onClick = {
                                                val newReminder = CustomReminder(amount = 1, unit = ReminderUnit.DAYS)
                                                val updatedList = customReminders + newReminder
                                                onCustomRemindersChanged(updatedList)
                                                triggerReschedule(remindersEnabled, updatedList)
                                                showAddReminderDialog = false
                                            },
                                            label = { Text("1 day") }
                                        )
                                        SuggestionChip(
                                            onClick = {
                                                val newReminder = CustomReminder(amount = 1, unit = ReminderUnit.WEEKS)
                                                val updatedList = customReminders + newReminder
                                                onCustomRemindersChanged(updatedList)
                                                triggerReschedule(remindersEnabled, updatedList)
                                                showAddReminderDialog = false
                                            },
                                            label = { Text("1 week") }
                                        )
                                    }

                                    HorizontalDivider()

                                    Text("Custom Reminder:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = customAmountInput,
                                            onValueChange = { customAmountInput = it.filter { char -> char.isDigit() } },
                                            label = { Text("Amount") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )

                                        var unitExpanded by remember { mutableStateOf(value = false) }
                                        ExposedDropdownMenuBox(
                                            expanded = unitExpanded,
                                            onExpandedChange = { unitExpanded = !unitExpanded },
                                            modifier = Modifier.weight(1.5f)
                                        ) {
                                            OutlinedTextField(
                                                value = selectedUnit.name.lowercase().replaceFirstChar { it.uppercase() },
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Unit") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                                            )
                                            ExposedDropdownMenu(
                                                expanded = unitExpanded,
                                                onDismissRequest = { unitExpanded = false }
                                            ) {
                                                ReminderUnit.entries.forEach { u ->
                                                    DropdownMenuItem(
                                                        text = { Text(u.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                                        onClick = {
                                                            selectedUnit = u
                                                            unitExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val amt = customAmountInput.toIntOrNull() ?: 1
                                        val newReminder = CustomReminder(amount = amt, unit = selectedUnit)
                                        val updatedList = customReminders + newReminder
                                        onCustomRemindersChanged(updatedList)
                                        triggerReschedule(remindersEnabled, updatedList)
                                        showAddReminderDialog = false
                                    }
                                ) {
                                    Text("Add")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAddReminderDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
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
                                Text(text = "Uses system theme if disabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = forceDark, onCheckedChange = onForceDarkChanged)
                    }

                    HorizontalDivider()

                    // 3. Database Errors Check / Reset
                    var hasChecked by remember { mutableStateOf(value = false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = if ((hasChecked) && (migrationError == null)) {
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
                                            text = migrationError,
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
                        if ((hasChecked) && (migrationError != null)) {
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
