package com.fkurevija.bioshot.feature.medication

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.ScheduleType
import com.fkurevija.bioshot.feature.medication.components.SyringeCalculatorCard
import com.fkurevija.bioshot.ui.theme.BioShotTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationSetupWizardScreen(
    initialMedication: MedicationEntity? = null,
    onSaveMedication: (String, String, ScheduleType, Int, DeviceType, String?) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(initialMedication?.name ?: "") }
    var dosage by remember { mutableStateOf(initialMedication?.dosage ?: "") }

    val initialPreset = if (initialMedication != null) {
        FrequencyPreset.entries.find { it.scheduleType == initialMedication.frequency } ?: FrequencyPreset.CUSTOM
    } else {
        FrequencyPreset.BIWEEKLY
    }
    var selectedPreset by remember { mutableStateOf(initialPreset) }

    val initialDays = initialMedication?.customIntervalDays ?: 14
    var customNumberText by remember { 
        mutableStateOf(
            if (initialMedication?.frequency == ScheduleType.CUSTOM) {
                if (initialDays % 30 == 0) (initialDays / 30).toString()
                else if (initialDays % 7 == 0) (initialDays / 7).toString()
                else initialDays.toString()
            } else {
                "4"
            }
        )
    }
    var customUnit by remember {
        mutableStateOf(
            if (initialDays % 30 == 0) CustomUnit.MONTHS
            else if (initialDays % 7 == 0) CustomUnit.WEEKS
            else CustomUnit.DAYS
        )
    }
    var deviceType by remember { mutableStateOf(initialMedication?.deviceType ?: DeviceType.PEN) }
    var notes by remember { mutableStateOf(initialMedication?.notes ?: "") }
    var hasLoadingDose by remember { mutableStateOf(false) }
    var loadingDoseOption by remember { mutableStateOf("Weeks 0, 2, 4 (Standard Biologic Induction)") }

    var freqExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var deviceExpanded by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && dosage.isNotBlank()

    val finalScheduleType = selectedPreset.scheduleType
    val finalIntervalDays = if (finalScheduleType == ScheduleType.CUSTOM) {
        val num = customNumberText.toIntOrNull()?.coerceAtLeast(1) ?: 1
        num * customUnit.daysMultiplier
    } else {
        selectedPreset.defaultDays
    }

    val saveAction = {
        if (isFormValid) {
            val combinedNotes = if (hasLoadingDose) {
                "Induction Phase: $loadingDoseOption. " + notes.trim()
            } else {
                notes.trim()
            }
            onSaveMedication(
                name.trim(),
                dosage.trim(),
                finalScheduleType,
                finalIntervalDays,
                deviceType,
                combinedNotes.ifBlank { null }
            )
        }
    }

    val applyPresetAction: (BiologicPreset) -> Unit = { preset ->
        name = preset.name
        if (dosage.isBlank()) dosage = preset.defaultDosage
        selectedPreset = when (preset.scheduleType) {
            ScheduleType.DAILY -> FrequencyPreset.DAILY
            ScheduleType.WEEKLY -> FrequencyPreset.WEEKLY
            ScheduleType.BIWEEKLY -> FrequencyPreset.BIWEEKLY
            ScheduleType.MONTHLY -> FrequencyPreset.MONTHLY
            ScheduleType.AS_NEEDED -> FrequencyPreset.AS_NEEDED
            ScheduleType.CUSTOM -> {
                if (preset.defaultDays % 30 == 0) {
                    customNumberText = (preset.defaultDays / 30).toString()
                    customUnit = CustomUnit.MONTHS
                } else if (preset.defaultDays % 7 == 0) {
                    customNumberText = (preset.defaultDays / 7).toString()
                    customUnit = CustomUnit.WEEKS
                } else {
                    customNumberText = preset.defaultDays.toString()
                    customUnit = CustomUnit.DAYS
                }
                FrequencyPreset.CUSTOM
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (initialMedication != null) "Edit Therapy" else "Add Biologics or GLP-1") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = saveAction,
                        enabled = isFormValid
                    ) {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = saveAction,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Rounded.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Medication", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = if (initialMedication != null) "Edit Medication Details" else "Therapy Setup Wizard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Track your prescribed biologic therapy or GLP-1 peptide treatment with precise pharmacokinetic estimates and reminders.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Syringe Calculator Card for GLP-1s / Peptides
            SyringeCalculatorCard(
                onApplyDosage = { calculatedDosage ->
                    dosage = calculatedDosage
                }
            )

            // Quick-select biologic presets
            PresetChipsSection("Quick Select Biologic", BIOLOGIC_PRESETS, name, applyPresetAction)

            // Quick-select GLP-1 peptide presets
            PresetChipsSection("Quick Select GLP-1 Peptide", GLP1_PRESETS, name, applyPresetAction)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medication Name *") },
                placeholder = { Text("e.g. Humira, Mounjaro (Tirzepatide)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Rounded.Medication, contentDescription = null) }
            )

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage * (e.g. 40mg / 2.5mg)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Rounded.Science, contentDescription = null) }
            )

            // Frequency Preset Dropdown
            ExposedDropdownMenuBox(
                expanded = freqExpanded,
                onExpandedChange = { freqExpanded = !freqExpanded }
            ) {
                OutlinedTextField(
                    value = selectedPreset.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequency Schedule *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = freqExpanded,
                    onDismissRequest = { freqExpanded = false }
                ) {
                    FrequencyPreset.entries.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(preset.label) },
                            onClick = {
                                selectedPreset = preset
                                freqExpanded = false
                            }
                        )
                    }
                }
            }

            if (selectedPreset == FrequencyPreset.CUSTOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customNumberText,
                        onValueChange = { customNumberText = it },
                        label = { Text("Every...") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = customUnit.label,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            CustomUnit.entries.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.label) },
                                    onClick = {
                                        customUnit = unit
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
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
                    label = { Text("Delivery Device Type *") },
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

            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(text = "Initial Loading / Induction Doses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = hasLoadingDose,
                            onCheckedChange = { hasLoadingDose = it }
                        )
                    }
                    if (hasLoadingDose) {
                        Text(
                            text = "Configures protocol for initial ramp-up / double dosing phases common in biologic therapy and GLP-1 titration schedules.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Instructions (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

@Composable
fun PresetChipsSection(
    title: String,
    presets: List<BiologicPreset>,
    selectedName: String,
    onPresetSelected: (BiologicPreset) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = selectedName == preset.name
                FilterChip(
                    selected = isSelected,
                    onClick = { onPresetSelected(preset) },
                    label = { Text(preset.name) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicationSetupWizardScreenPreview() {
    BioShotTheme {
        MedicationSetupWizardScreen(
            onSaveMedication = { _, _, _, _, _, _ -> },
            onNavigateBack = {}
        )
    }
}
