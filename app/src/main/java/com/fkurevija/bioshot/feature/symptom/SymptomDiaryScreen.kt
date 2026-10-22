package com.fkurevija.bioshot.feature.symptom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.SymptomEntryEntity
import com.fkurevija.bioshot.ui.theme.BioShotTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomDiaryScreen(
    symptomEntries: List<SymptomEntryEntity>,
    injectionLogs: List<InjectionLogEntity>,
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

            // Add Symptom Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "New Symptom Entry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = symptomName,
                        onValueChange = { symptomName = it },
                        label = { Text("Symptom Title (e.g., Knee joint stiffness)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Tags selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Category Tag",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val tagRows = commonTags.chunked(3)
                        tagRows.forEach { rowTags ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowTags.forEach { tag ->
                                    FilterChip(
                                        selected = selectedTag == tag,
                                        onClick = { selectedTag = tag },
                                        label = { Text(tag) }
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        Text(
                            text = "Severity: ${severity.toInt()} / 10",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = severity,
                            onValueChange = { severity = it },
                            valueRange = 1f..10f,
                            steps = 9
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Observations") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            if (symptomName.isNotBlank()) {
                                onSaveSymptomEntry(severity.toInt(), symptomName, notes.ifBlank { null }, listOf(selectedTag))
                                symptomName = ""
                                notes = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = symptomName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Symptom Entry")
                    }
                }
            }

            // Timeline & Correlation Section
            Text(
                text = "Linked Timeline (Symptoms & Injections)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (symptomEntries.isEmpty() && injectionLogs.isEmpty()) {
                Text(
                    text = "No logs or symptoms recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Combine and sort by timestamp descending
                val combinedItems = remember(symptomEntries, injectionLogs) {
                    val list = mutableListOf<TimelineItem>()
                    symptomEntries.forEach { s ->
                        list.add(TimelineItem(s.timestamp, TimelineType.SYMPTOM, s.symptomName, "Severity: ${s.severity}/10 - ${s.notes ?: ""}", s.id))
                    }
                    injectionLogs.forEach { i ->
                        list.add(TimelineItem(i.timestamp, TimelineType.INJECTION, "Injection: ${i.site.name.replace("_", " ")}", "Dosage: ${i.dosageGiven} | Pain: ${i.painLevel ?: 0}/10", i.id))
                    }
                    list.sortedByDescending { it.timestamp }
                }

                combinedItems.forEach { item ->
                    TimelineCard(item = item)
                }
            }
        }
    }
}

enum class TimelineType {
    SYMPTOM, INJECTION
}

data class TimelineItem(
    val timestamp: Long,
    val type: TimelineType,
    val title: String,
    val details: String,
    val id: Long
)

@Composable
fun TimelineCard(item: TimelineItem) {
    val locale = androidx.compose.ui.platform.LocalLocale.current.platformLocale
    val dateFormat = remember(locale) { SimpleDateFormat("MMM dd, yyyy - HH:mm", locale) }
    val formattedDate = dateFormat.format(Date(item.timestamp))

    val containerColor = if (item.type == TimelineType.INJECTION) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (item.type == TimelineType.INJECTION) Icons.Rounded.Medication else Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = if (item.type == TimelineType.INJECTION) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = if (item.type == TimelineType.INJECTION) "Injection" else "Symptom",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Text(
                text = item.details,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SymptomDiaryScreenPreview() {
    BioShotTheme {
        SymptomDiaryScreen(
            symptomEntries = listOf(
                SymptomEntryEntity(
                    id = 1,
                    timestamp = System.currentTimeMillis(),
                    severity = 4,
                    symptomName = "Joint Stiffness",
                    notes = "Morning stiffness in hands"
                )
            ),
            injectionLogs = emptyList(),
            onSaveSymptomEntry = { _, _, _, _ -> },
            onDeleteSymptomEntry = {}
        )
    }
}
