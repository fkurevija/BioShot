package com.fkurevija.bioshot.feature.settings

import android.content.Intent
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
import com.fkurevija.bioshot.core.database.DatabaseBackupManager
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.database.SymptomEntryEntity
import com.fkurevija.bioshot.core.notifications.NotificationScheduler
import com.fkurevija.bioshot.core.schedule.ScheduleEngine
import com.fkurevija.bioshot.feature.settings.components.DoctorReportSection
import com.fkurevija.bioshot.ui.theme.BioShotTheme
import kotlinx.coroutines.launch

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

                    HorizontalDivider()

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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    val backupFile = DatabaseBackupManager.createManualBackup(context)
                                    if (backupFile != null) {
                                        android.widget.Toast.makeText(context, "Backup created successfully!", android.widget.Toast.LENGTH_SHORT).show()
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
