package com.fkurevija.bioshot.feature.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    val medications by viewModel.medications.collectAsStateWithLifecycle()
    val injectionLogs by viewModel.injectionLogs.collectAsStateWithLifecycle()
    val symptomEntries by viewModel.symptomEntries.collectAsStateWithLifecycle()

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
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Dashboard -> {
                    DashboardScreen(
                        medications = medications,
                        injectionLogs = injectionLogs,
                        onQuickLogClick = { currentScreen = Screen.InjectionLog },
                        onNavigateToMedications = { currentScreen = Screen.MedicationList },
                        onNavigateToLog = { currentScreen = Screen.InjectionLog }
                    )
                }
                is Screen.MedicationList -> {
                    MedicationListScreen(
                        medications = medications,
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
                        medications = medications,
                        injectionLogs = injectionLogs,
                        onSaveInjectionLog = { medId, site, dosage, lot, notes, pain, sideEffects ->
                            viewModel.insertInjectionLog(medId, site, dosage, lot, notes, pain, sideEffects)
                        },
                        onDeleteInjectionLog = { viewModel.deleteInjectionLog(it) }
                    )
                }
                is Screen.SymptomDiary -> {
                    SymptomDiaryScreen(
                        symptomEntries = symptomEntries,
                        injectionLogs = injectionLogs,
                        onSaveSymptomEntry = { severity, name, notes, tags ->
                            viewModel.insertSymptomEntry(severity, name, notes, tags)
                        },
                        onDeleteSymptomEntry = { viewModel.deleteSymptomEntry(it) }
                    )
                }
                is Screen.PkChart -> {
                    PkChartScreen(
                        medications = medications,
                        injectionLogs = injectionLogs
                    )
                }
                is Screen.DoctorExportAndSettings -> {
                    DoctorExportAndSettingsScreen(
                        medications = medications,
                        injectionLogs = injectionLogs,
                        symptomEntries = symptomEntries,
                        forceDark = viewModel.forceDark.collectAsStateWithLifecycle().value,
                        remindersEnabled = viewModel.remindersEnabled.collectAsStateWithLifecycle().value,
                        onForceDarkChanged = { viewModel.setForceDark(it) },
                        onRemindersChanged = { viewModel.setRemindersEnabled(it) }
                    )
                }
            }
        }
    }
}
