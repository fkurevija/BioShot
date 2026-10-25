package com.fkurevija.bioshot.feature.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.database.SymptomEntryEntity
import com.fkurevija.bioshot.feature.chart.PkChartScreen
import com.fkurevija.bioshot.feature.dashboard.DashboardScreen
import com.fkurevija.bioshot.feature.log.InjectionLogScreen
import com.fkurevija.bioshot.feature.medication.MedicationListScreen
import com.fkurevija.bioshot.feature.medication.MedicationSetupWizardScreen
import com.fkurevija.bioshot.feature.settings.DoctorExportAndSettingsScreen
import com.fkurevija.bioshot.feature.symptom.SymptomDiaryScreen
import com.fkurevija.bioshot.feature.viewmodel.BioShotViewModel

sealed class NavigationItem(val route: Screen, val label: String, val icon: ImageVector) {
    data object Dashboard : NavigationItem(Screen.Dashboard, "Dashboard", Icons.Rounded.Dashboard)
    data object Medications : NavigationItem(Screen.MedicationList, "Medications", Icons.Rounded.Medication)
    data object InjectionLog : NavigationItem(Screen.InjectionLog, "Body Map", Icons.Rounded.LocationOn)
    data object SymptomDiary : NavigationItem(Screen.SymptomDiary, "Symptoms", Icons.Rounded.Healing)
    data object PkChart : NavigationItem(Screen.PkChart, "PK Curve", Icons.AutoMirrored.Rounded.ShowChart)}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioShotNavGraph(
    viewModel: BioShotViewModel,
    modifier: Modifier = Modifier,
    medications: List<MedicationEntity> = emptyList(),
    injectionLogs: List<InjectionLogEntity> = emptyList(),
    symptomEntries: List<SymptomEntryEntity> = emptyList()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var editingMedication by remember { mutableStateOf<MedicationEntity?>(null) }

    // Collect state with lifecycle so Room database changes persist and update UI instantly
    val viewModelMedications = viewModel.medications.collectAsStateWithLifecycle().value
    val viewModelLogs = viewModel.injectionLogs.collectAsStateWithLifecycle().value
    val viewModelSymptoms = viewModel.symptomEntries.collectAsStateWithLifecycle().value
    val viewModelWeights = viewModel.weightLogs.collectAsStateWithLifecycle().value

    val effectiveMedications = if (medications.isNotEmpty()) medications else viewModelMedications
    val effectiveInjectionLogs = if (injectionLogs.isNotEmpty()) injectionLogs else viewModelLogs
    val effectiveSymptomEntries = if (symptomEntries.isNotEmpty()) symptomEntries else viewModelSymptoms

    val navItems = listOf(
        NavigationItem.Dashboard,
        NavigationItem.Medications,
        NavigationItem.InjectionLog,
        NavigationItem.SymptomDiary,
        NavigationItem.PkChart
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                tonalElevation = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentScreen == item.route,
                        onClick = { currentScreen = item.route },
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                        label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (currentScreen) {
                is Screen.Dashboard -> {
                    DashboardScreen(
                        medications = effectiveMedications,
                        injectionLogs = effectiveInjectionLogs,
                        weightLogs = viewModelWeights,
                        onQuickLogClick = { currentScreen = Screen.InjectionLog },
                        onNavigateToMedications = { currentScreen = Screen.MedicationList },
                        onNavigateToLog = { currentScreen = Screen.InjectionLog },
                        onNavigateToSettings = { currentScreen = Screen.DoctorExportAndSettings },
                        onAddWeightLog = { w, unit, fat, waist, notes ->
                            viewModel.insertWeightLog(w, unit, fat, waist, notes)
                        },
                        innerPadding = PaddingValues(top = 0.dp)
                    )
                }
                is Screen.MedicationList -> {
                    MedicationListScreen(
                        medications = effectiveMedications,
                        onAddMedicationClick = {
                            editingMedication = null
                            currentScreen = Screen.MedicationSetupWizard
                        },
                        onEditMedication = { medication ->
                            editingMedication = medication
                            currentScreen = Screen.MedicationSetupWizard
                        },
                        onDeleteMedication = { viewModel.deleteMedication(it) },
                        onNavigateToSettings = { currentScreen = Screen.DoctorExportAndSettings }
                    )
                }
                is Screen.MedicationSetupWizard -> {
                    MedicationSetupWizardScreen(
                        initialMedication = editingMedication,
                        onSaveMedication = { name, dosage, freq, customDays, device, notes ->
                            val editing = editingMedication
                            if (editing != null) {
                                viewModel.updateMedication(
                                    editing.copy(
                                        name = name,
                                        dosage = dosage,
                                        frequency = freq,
                                        customIntervalDays = customDays,
                                        deviceType = device,
                                        notes = notes
                                    )
                                )
                            } else {
                                viewModel.insertMedication(name, dosage, freq, customDays, device, notes)
                            }
                            editingMedication = null
                            currentScreen = Screen.MedicationList
                        },
                        onNavigateBack = {
                            editingMedication = null
                            currentScreen = Screen.MedicationList
                        }
                    )
                }
                is Screen.InjectionLog -> {
                    InjectionLogScreen(
                        medications = effectiveMedications,
                        injectionLogs = effectiveInjectionLogs,
                        onSaveInjectionLog = { medId, site, dosage, lot, notes, pain, sideEffects, timestamp ->
                            viewModel.insertInjectionLog(medId, site, dosage, lot, notes, pain, sideEffects, timestamp)
                        },
                        onUpdateInjectionLog = { viewModel.updateInjectionLog(it) },
                        onDeleteInjectionLog = { viewModel.deleteInjectionLog(it) },
                        onNavigateToSettings = { currentScreen = Screen.DoctorExportAndSettings }
                    )
                }
                is Screen.SymptomDiary -> {
                    SymptomDiaryScreen(
                        symptomEntries = effectiveSymptomEntries,
                        injectionLogs = effectiveInjectionLogs,
                        medications = effectiveMedications,
                        onSaveSymptomEntry = { severity, name, notes, tags ->
                            viewModel.insertSymptomEntry(severity, name, notes, tags)
                        },
                        onDeleteSymptomEntry = { entry ->
                            viewModel.deleteSymptomEntry(entry)
                        },
                        onNavigateToSettings = { currentScreen = Screen.DoctorExportAndSettings }
                    )
                }
                is Screen.PkChart -> {
                    PkChartScreen(
                        medications = effectiveMedications,
                        injectionLogs = effectiveInjectionLogs,
                        onNavigateToSettings = { currentScreen = Screen.DoctorExportAndSettings }
                    )
                }
                is Screen.DoctorExportAndSettings -> {
                    DoctorExportAndSettingsScreen(
                        medications = effectiveMedications,
                        injectionLogs = effectiveInjectionLogs,
                        symptomEntries = effectiveSymptomEntries,
                        forceDark = viewModel.forceDark.collectAsStateWithLifecycle().value,
                        remindersEnabled = viewModel.remindersEnabled.collectAsStateWithLifecycle().value,
                        migrationError = viewModel.migrationError.collectAsStateWithLifecycle().value,
                        onForceDarkChanged = { viewModel.setForceDark(it) },
                        onRemindersChanged = { viewModel.setRemindersEnabled(it) },
                        onCheckDatabaseErrors = { viewModel.checkDatabaseErrors() },
                        onResetDatabase = { viewModel.resetDatabase() }
                    )
                }
            }
        }
    }
}
