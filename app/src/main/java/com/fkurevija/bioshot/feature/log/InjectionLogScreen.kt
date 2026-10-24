package com.fkurevija.bioshot.feature.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjectionLogScreen(
    medications: List<MedicationEntity>,
    injectionLogs: List<InjectionLogEntity>,
    onSaveInjectionLog: (Long, InjectionSite, String, String?, String?, Int?, List<String>) -> Unit,
    onDeleteInjectionLog: (InjectionLogEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSite by remember { mutableStateOf(InjectionSite.ABDOMEN_LEFT) }
    var selectedMedicationId by remember { mutableStateOf(medications.firstOrNull()?.id ?: 0L) }
    var dosageGiven by remember { mutableStateOf(medications.firstOrNull()?.dosage ?: "40mg") }
    var lotNumber by remember { mutableStateOf("") }
    var painLevel by remember { mutableStateOf(1f) }
    var notes by remember { mutableStateOf("") }

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
                .padding(16.dp),
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
                                    emptyList()
                                )
                                lotNumber = ""
                                notes = ""
                            }
                        }
                    )
                     OutlinedTextField(
                         value = notes,
                         onValueChange = { notes = it },
                         label = { Text("Notes / Side Effects") },
                         modifier = Modifier.fillMaxWidth(),
                         maxLines = 2
                     )
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
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(injectionLogs) { log ->
                        InjectionLogItemCard(
                            log = log,
                            onDelete = { onDeleteInjectionLog(log) }
                        )
                    }
                }
            }
        }
    }
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
            onSaveInjectionLog = { _, _, _, _, _, _, _ -> },
            onDeleteInjectionLog = {}
        )
    }
}
