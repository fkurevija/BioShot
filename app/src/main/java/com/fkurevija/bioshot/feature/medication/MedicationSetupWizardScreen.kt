package com.fkurevija.bioshot.feature.medication

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.ScheduleType
import com.fkurevija.bioshot.ui.theme.BioShotTheme

/** Bundled biologic presets shown as quick-select chips. */
private data class BiologicPreset(val name: String, val defaultDosage: String)

private val BIOLOGIC_PRESETS = listOf(
    BiologicPreset("Humira (Adalimumab)", "40mg"),
    BiologicPreset("Enbrel (Etanercept)", "50mg"),
    BiologicPreset("Stelara (Ustekinumab)", "45mg"),
    BiologicPreset("Cosentyx (Secukinumab)", "300mg"),
    BiologicPreset("Dupixent (Dupilumab)", "300mg"),
    BiologicPreset("Simponi (Golimumab)", "50mg"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationSetupWizardScreen(
    onSaveMedication: (String, String, ScheduleType, DeviceType, String?) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(ScheduleType.BIWEEKLY) }
    var deviceType by remember { mutableStateOf(DeviceType.PEN) }
    var notes by remember { mutableStateOf("") }

    var freqExpanded by remember { mutableStateOf(false) }
    var deviceExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Add Biologic Medication") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Medication Setup Wizard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Enter your prescribed biologic therapy details to configure reminders, dosing intervals, and PK tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Quick-select biologic presets
            Text(
                text = "Quick Select Biologic",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BIOLOGIC_PRESETS.forEach { preset ->
                    val isSelected = name == preset.name
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            name = preset.name
                            if (dosage.isBlank()) dosage = preset.defaultDosage
                        },
                        label = { Text(preset.name) }
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medication Name") },
                placeholder = { Text("e.g. Humira, Simponi (Golimumab)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Rounded.Medication, contentDescription = null) }
            )

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage (e.g. 40mg / 1ml)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Rounded.Science, contentDescription = null) }
            )

            // Frequency Dropdown
            ExposedDropdownMenuBox(
                expanded = freqExpanded,
                onExpandedChange = { freqExpanded = !freqExpanded }
            ) {
                OutlinedTextField(
                    value = frequency.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequency Schedule") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = freqExpanded,
                    onDismissRequest = { freqExpanded = false }
                ) {
                    ScheduleType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name.replace("_", " ")) },
                            onClick = {
                                frequency = type
                                freqExpanded = false
                            }
                        )
                    }
                }
            }

            // Device Type Dropdown
            ExposedDropdownMenuBox(
                expanded = deviceExpanded,
                onExpandedChange = { deviceExpanded = !deviceExpanded }
            ) {
                OutlinedTextField(
                    value = deviceType.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Injection Device Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deviceExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = deviceExpanded,
                    onDismissRequest = { deviceExpanded = false }
                ) {
                    DeviceType.entries.forEach { device ->
                        DropdownMenuItem(
                            text = { Text(device.name.replace("_", " ")) },
                            onClick = {
                                deviceType = device
                                deviceExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Optional Notes / Instructions") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && dosage.isNotBlank()) {
                        onSaveMedication(name, dosage, frequency, deviceType, notes.ifBlank { null })
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && dosage.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(imageVector = Icons.Rounded.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Medication")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicationSetupWizardScreenPreview() {
    BioShotTheme {
        MedicationSetupWizardScreen(
            onSaveMedication = { _, _, _, _, _ -> },
            onNavigateBack = {}
        )
    }
}
