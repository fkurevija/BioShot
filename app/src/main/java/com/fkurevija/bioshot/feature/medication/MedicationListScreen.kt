package com.fkurevija.bioshot.feature.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.ScheduleType
import com.fkurevija.bioshot.ui.theme.BioShotTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    medications: List<MedicationEntity>,
    onAddMedicationClick: () -> Unit,
    onDeleteMedication: (MedicationEntity) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("My Medications") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicationClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Medication")
            }
        }
    ) { innerPadding ->
        if (medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Medication,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "No medications added yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onAddMedicationClick) {
                        Text("Add Your First Medication")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(medications) { medication ->
                    MedicationCard(
                        medication = medication,
                        onDelete = { onDeleteMedication(medication) }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationCard(
    medication: MedicationEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete Medication",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BadgeItem(label = "Dosage", value = medication.dosage)
                BadgeItem(label = "Frequency", value = medication.frequency.name)
                BadgeItem(label = "Device", value = medication.deviceType.name.replace("_", " "))
            }

            if (!medication.notes.isNullOrBlank()) {
                Text(
                    text = "Notes: ${medication.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BadgeItem(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicationListScreenPreview() {
    BioShotTheme {
        MedicationListScreen(
            medications = listOf(
                MedicationEntity(
                    id = 1,
                    name = "Humira (Adalimumab)",
                    dosage = "40mg",
                    frequency = ScheduleType.BIWEEKLY,
                    deviceType = DeviceType.PEN,
                    notes = "Take with food"
                )
            ),
            onAddMedicationClick = {},
            onDeleteMedication = {},
            onNavigateToSettings = {}
        )
    }
}
