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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (initialMedication != null) "Edit Biologic Medication" else "Add Biologic Medication") },
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
                text = if (initialMedication != null) "Edit Medication Details" else "Medication Setup Wizard",
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
                            when (preset.scheduleType) {
                                ScheduleType.DAILY -> selectedPreset = FrequencyPreset.DAILY
                                ScheduleType.WEEKLY -> selectedPreset = FrequencyPreset.WEEKLY
                                ScheduleType.BIWEEKLY -> selectedPreset = FrequencyPreset.BIWEEKLY
                                ScheduleType.MONTHLY -> selectedPreset = FrequencyPreset.MONTHLY
                                ScheduleType.AS_NEEDED -> selectedPreset = FrequencyPreset.AS_NEEDED
                                ScheduleType.CUSTOM -> {
                                    selectedPreset = FrequencyPreset.CUSTOM
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
                                }
                            }
                        },
                        label = { Text(preset.name) }
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medication Name *") },
                placeholder = { Text("e.g. Humira, Simponi (Golimumab)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Rounded.Medication, contentDescription = null) }
            )

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage * (e.g. 40mg / 1ml)") },
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

            // Custom Interval Inputs (Only shown if Custom preset is selected)
            if (selectedPreset == FrequencyPreset.CUSTOM) {
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Custom Schedule Configuration",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customNumberText,
                                onValueChange = { customNumberText = it },
                                label = { Text("Every...") },
                                placeholder = { Text("4") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            // Unit Dropdown (Days, Weeks, Months)
                            ExposedDropdownMenuBox(
                                expanded = unitExpanded,
                                onExpandedChange = { unitExpanded = !unitExpanded },
                                modifier = Modifier.weight(1.2f)
                            ) {
                                OutlinedTextField(
                                    value = customUnit.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Unit") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    singleLine = true
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

                        val num = customNumberText.toIntOrNull() ?: 0
                        if (num > 0) {
                            val hintMessage = when (customUnit) {
                                CustomUnit.DAYS -> "Schedule: Every $num ${if (num == 1) "day" else "days"}"
                                CustomUnit.WEEKS -> {
                                    val totalDays = num * 7
                                    "Schedule: Every $num ${if (num == 1) "week" else "weeks"} ($totalDays days between doses)"
                                }
                                CustomUnit.MONTHS -> {
                                    val totalDays = num * 30
                                    val approxWeeks = totalDays / 7.0
                                    "Schedule: Every $num ${if (num == 1) "month" else "months"} (~${String.format("%.1f", approxWeeks)} weeks / $totalDays days between doses)"
                                }
                            }
                            Text(
                                text = hintMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Loading-Dose / Induction Phase Setup Card
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "Initial Loading Doses (Induction)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = hasLoadingDose,
                            onCheckedChange = { hasLoadingDose = it }
                        )
                    }

                    if (hasLoadingDose) {
                        Text(
                            text = "Configure initial loading doses before transitioning to your regular maintenance schedule.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = loadingDoseOption,
                            onValueChange = { loadingDoseOption = it },
                            label = { Text("Induction / Loading Schedule") },
                            placeholder = { Text("e.g. Weeks 0, 2, 4 then maintenance") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
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
                placeholder = { Text("e.g. Take with food, inject in abdomen") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
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
