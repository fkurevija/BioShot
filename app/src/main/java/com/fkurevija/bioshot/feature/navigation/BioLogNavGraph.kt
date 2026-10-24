package com.fkurevija.bioshot.feature.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
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
    data object PkChart : NavigationItem(Screen.PkChart, "PK Curve", Icons.AutoMirrored.Rounded.ShowChart)
    data object Settings : NavigationItem(Screen.DoctorExportAndSettings, "Export & Settings", Icons.Rounded.Settings)
}

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

    // Use passed parameters or fall back to viewModel state
    val effectiveMedications = if (medications.isNotEmpty()) medications else viewModel.medications.value
    val effectiveInjectionLogs = if (injectionLogs.isNotEmpty()) injectionLogs else viewModel.injectionLogs.value
    val effectiveSymptomEntries = if (symptomEntries.isNotEmpty()) symptomEntries else viewModel.symptomEntries.value

    val navItems = listOf(
        NavigationItem.Dashboard,
        NavigationItem.Medications,
        NavigationItem.InjectionLog,
        NavigationItem.SymptomDiary,
        NavigationItem.PkChart,
        NavigationItem.Settings
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
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
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (currentScreen) {
                is Screen.Dashboard -> {
                    DashboardScreen(
                        medications = effectiveMedications,
                        injectionLogs = effectiveInjectionLogs,
                        onQuickLogClick = { currentScreen = Screen.InjectionLog },
                        onNavigateToMedications = { currentScreen = Screen.MedicationList },
                        onNavigateToLog = { currentScreen = Screen.InjectionLog },
                        innerPadding = innerPadding
                    )
                }
                is Screen.MedicationList -> {
                    MedicationListScreen(
                        medications = effectiveMedications,
                        onAddMedicationClick = { currentScreen = Screen.MedicationSetupWizard },
                        onDeleteMedication = { viewModel.deleteMedication(it) }
                    )
                }
                is Screen.MedicationSetupWizard -> {
                    MedicationSetupWizardScreen(
                        onSaveMedication = { name, dosage, freq, device, notes ->
                            viewModel.insertMedication(name, dosage, freq, device, notes)
                            currentScreen = Screen.MedicationList
                        },
                        onNavigateBack = { currentScreen = Screen.MedicationList }
                    )
                }
                is Screen.InjectionLog -> {
                    InjectionLogScreen(
                        medications = effectiveMedications,
                        injectionLogs = effectiveInjectionLogs,
                        onSaveInjectionLog = { medId, site, dosage, lot, notes, pain, sideEffects ->
                            viewModel.insertInjectionLog(medId, site, dosage, lot, notes, pain, sideEffects)
                        },
                        onDeleteInjectionLog = { viewModel.deleteInjectionLog(it) }
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
                        }
                    )
                }
                is Screen.PkChart -> {
                    PkChartScreen(
                        medications = effectiveMedications,
                        injectionLogs = effectiveInjectionLogs
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
                        onResetDatabase = { viewModel.resetDatabase() }
                    )
                }
            }
        }
    }
}
