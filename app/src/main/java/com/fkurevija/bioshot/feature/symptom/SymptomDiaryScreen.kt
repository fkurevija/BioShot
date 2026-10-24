package com.fkurevija.bioshot.feature.symptom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.database.SymptomEntryEntity
import com.fkurevija.bioshot.feature.symptom.components.SymptomForm
import com.fkurevija.bioshot.feature.symptom.components.TimelineCard
import com.fkurevija.bioshot.feature.symptom.components.TimelineItem
import com.fkurevija.bioshot.feature.symptom.components.TimelineType
import com.fkurevija.bioshot.ui.theme.BioShotTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomDiaryScreen(
    symptomEntries: List<SymptomEntryEntity>,
    injectionLogs: List<InjectionLogEntity>,
    medications: List<MedicationEntity>,
    onSaveSymptomEntry: (Int, String, String?, List<String>) -> Unit,
    onDeleteSymptomEntry: (SymptomEntryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var symptomName by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(3f) }
    var notes by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Joint Pain") }

    val commonTags = listOf("Joint Pain", "Fatigue", "Skin Flare", "Gastrointestinal", "Headache", "Fever")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Symptom Diary & Timeline") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Track Symptoms & Flare-ups",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Log your symptoms and correlate them with your injection timeline and medication efficacy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Symptom Form
            SymptomForm(
                symptomName = symptomName,
                severity = severity,
                notes = notes,
                selectedTag = selectedTag,
                commonTags = commonTags,
                onSymptomNameChange = { symptomName = it },
                onSeverityChange = { severity = it },
                onNotesChange = { notes = it },
                onTagSelected = { selectedTag = it },
                onSave = {
                    if (symptomName.isNotBlank()) {
                        onSaveSymptomEntry(severity.toInt(), symptomName, notes.ifBlank { null }, listOf(selectedTag))
                        symptomName = ""
                        notes = ""
                    }
                }
            )

            // Timeline & Correlation Section
            Text(
                text = "Timeline & Correlation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View your injection and symptom history in chronological order.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (symptomEntries.isEmpty() && injectionLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EventNote,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No symptoms or injections recorded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(symptomEntries) { entry ->
                        TimelineCard(
                            item = TimelineItem(
                                timestamp = entry.timestamp,
                                type = TimelineType.SYMPTOM,
                                title = entry.symptomName,
                                details = "Severity: ${entry.severity}/10",
                                id = entry.id
                            ),
                            onDelete = { onDeleteSymptomEntry(entry) }
                        )
                    }

                    items(injectionLogs) { log ->
                        val medicationName = medications.firstOrNull { it.id == log.medicationId }?.name ?: "Unknown"
                        TimelineCard(
                            item = TimelineItem(
                                timestamp = log.timestamp,
                                type = TimelineType.INJECTION,
                                title = medicationName,
                                details = "Site: ${log.site.name}",
                                id = log.id
                            )
                        )
                    }
                }
            }
        }
    }
}
