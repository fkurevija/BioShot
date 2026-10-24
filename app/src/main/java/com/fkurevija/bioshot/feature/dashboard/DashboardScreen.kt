package com.fkurevija.bioshot.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.core.model.ScheduleType
import com.fkurevija.bioshot.core.schedule.ScheduleEngine
import com.fkurevija.bioshot.feature.dashboard.components.NextDoseCountdownCard
import com.fkurevija.bioshot.feature.dashboard.components.LatestInjectionInfoCard
import com.fkurevija.bioshot.feature.dashboard.components.CountdownUnit
import com.fkurevija.bioshot.ui.theme.BioShotTheme
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    medications: List<MedicationEntity>,
    injectionLogs: List<InjectionLogEntity>,
    onQuickLogClick: () -> Unit,
    onNavigateToMedications: () -> Unit,
    onNavigateToLog: () -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val scrollState = rememberScrollState()
    val latestMedication = medications.firstOrNull()
    val latestLog = injectionLogs.firstOrNull()

    // Next dose calculation
    val nextDoseTime = remember(latestMedication, latestLog) {
        if (latestMedication == null) return@remember null
        val lastTime = latestLog?.timestamp ?: latestMedication.createdAt
        val res = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = lastTime,
            scheduleType = latestMedication.frequency
        )
        res.nextTimestamp
    }

    val now = System.currentTimeMillis()
    val diffMillis = if (nextDoseTime != null) nextDoseTime - now else 0L
    val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis)
    val hoursRemaining = TimeUnit.MILLISECONDS.toHours(diffMillis) % 24

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header / Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BioShot Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Track your biologic therapy & wellness",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalIconButton(onClick = onNavigateToMedications) {
                    Icon(
                        imageVector = Icons.Rounded.Medication,
                        contentDescription = "Manage Medications"
                    )
                }
            }

            // Next Dose Countdown Card
            NextDoseCountdownCard(
                latestMedication = latestMedication,
                latestLog = latestLog,
                diffMillis = diffMillis,
                onNavigateToMedications = onNavigateToMedications,
                onQuickLogClick = onQuickLogClick
            )

            // Last Injection Site Reminder Card
            LatestInjectionInfoCard(
                latestLog = latestLog,
                onNavigateToLog = onNavigateToLog
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    BioShotTheme {
        DashboardScreen(
            medications = listOf(
                MedicationEntity(
                    id = 1,
                    name = "Humira (Adalimumab)",
                    dosage = "40mg",
                    frequency = ScheduleType.BIWEEKLY,
                    deviceType = DeviceType.PEN
                )
            ),
            injectionLogs = listOf(
                InjectionLogEntity(
                    id = 1,
                    medicationId = 1,
                    timestamp = System.currentTimeMillis() - 86400000L * 3,
                    site = InjectionSite.ABDOMEN_LEFT,
                    dosageGiven = "40mg"
                )
            ),
            onQuickLogClick = {},
            onNavigateToMedications = {},
            onNavigateToLog = {}
        )
    }
}
