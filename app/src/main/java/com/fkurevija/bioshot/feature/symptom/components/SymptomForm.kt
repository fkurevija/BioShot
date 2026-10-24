package com.fkurevija.bioshot.feature.symptom.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** New symptom entry form used in SymptomDiaryScreen. */
@Composable
fun SymptomForm(
    symptomName: String,
    severity: Float,
    notes: String,
    selectedTag: String,
    commonTags: List<String>,
    onSymptomNameChange: (String) -> Unit,
    onSeverityChange: (Float) -> Unit,
    onNotesChange: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
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
                onValueChange = onSymptomNameChange,
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
                                onClick = { onTagSelected(tag) },
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
                    onValueChange = onSeverityChange,
                    valueRange = 1f..10f,
                    steps = 9
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notes / Observations") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save Symptom")
            }
        }
    }
}

