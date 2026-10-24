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
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.database.SymptomEntryEntity
import com.fkurevija.bioshot.ui.theme.BioShotTheme
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
    migrationError: String? = null,
    onForceDarkChanged: (Boolean) -> Unit = {},
    onRemindersChanged: (Boolean) -> Unit = {},
    onCheckDatabaseErrors: () -> Unit = {},
    onResetDatabase: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val summaryText = remember(medications, injectionLogs, symptomEntries) {
        val sb = StringBuilder()
        sb.append("=== BIOSHOT THERAPY & SYMPTOM REPORT ===\n\n")
        sb.append("--- MEDICATIONS ---\n")
        medications.forEach { m ->
            sb.append("- ${m.name}: ${m.dosage}, Frequency: ${m.frequency.name}, Device: ${m.deviceType.name}\n")
        }
        sb.append("\n--- INJECTION HISTORY ---\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        injectionLogs.forEach { l ->
            val dateStr = dateFormat.format(Date(l.timestamp))
            sb.append("- Date: $dateStr | Site: ${l.site.name} | Dosage: ${l.dosageGiven} | Pain: ${l.painLevel ?: 0}/10 | Lot: ${l.lotNumber ?: "N/A"}\n")
        }
        sb.append("\n--- SYMPTOM DIARY ---\n")
        symptomEntries.forEach { s ->
            val dateStr = dateFormat.format(Date(s.timestamp))
            sb.append("- Date: $dateStr | Symptom: ${s.symptomName} | Severity: ${s.severity}/10 | Notes: ${s.notes ?: "None"}\n")
        }
        sb.toString()
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
            Text(
                text = "Doctor Visit Report Export",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Export your complete injection history, site rotation records, and symptom diary as a structured report for your rheumatologist or gastroenterologist.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Export Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Generate Shareable Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Includes ${medications.size} medications, ${injectionLogs.size} injections, and ${symptomEntries.size} symptom records.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_SUBJECT, "BIOSHOT THERAPY & Symptom Report")
                                putExtra(Intent.EXTRA_TEXT, summaryText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Rounded.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Report via Email / Apps")
                    }
                }
            }

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
                                Text(text = "Notify when next dose is due", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = remindersEnabled, onCheckedChange = onRemindersChanged)
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
