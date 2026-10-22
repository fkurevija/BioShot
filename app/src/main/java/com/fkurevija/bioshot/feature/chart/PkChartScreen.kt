package com.fkurevija.bioshot.feature.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.core.model.ScheduleType
import com.fkurevija.bioshot.core.pk.PkCalculator
import com.fkurevija.bioshot.ui.theme.BioShotTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PkChartScreen(
    medications: List<MedicationEntity>,
    injectionLogs: List<InjectionLogEntity>,
    modifier: Modifier = Modifier
) {
    val latestMed = medications.firstOrNull()
    val halfLifeHours = 336.0 // e.g., ~14 days for adalimumab standard half-life

    // Build dose events from injection logs
    val doseEvents = remember(injectionLogs) {
        injectionLogs.map { log ->
            // Parse numerical amount e.g. "40mg" -> 40.0
            val amountNum = log.dosageGiven.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 40.0
            PkCalculator.DoseEvent(
                timestamp = log.timestamp,
                doseAmount = amountNum,
                bioavailability = 0.85
            )
        }
    }

    // Generate curve over last 30 days to next 30 days
    val now = System.currentTimeMillis()
    val startTime = now - (30L * 24 * 60 * 60 * 1000)
    val endTime = now + (30L * 24 * 60 * 60 * 1000)

    val curvePoints = remember(doseEvents) {
        if (doseEvents.isEmpty()) {
            emptyList()
        } else {
            PkCalculator.generateCurve(
                doses = doseEvents,
                startTimeMs = startTime,
                endTimeMs = endTime,
                stepCount = 120,
                halfLifeHours = halfLifeHours,
                absorptionHalfLifeHours = 24.0
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Pharmacokinetic (PK) Curve") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Mandatory Disclaimer Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "Disclaimer",
                        modifier = Modifier.size(32.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "ESTIMATE ONLY DISCLAIMER",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = PkCalculator.DISCLAIMER_TEXT,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Text(
                text = "Estimated Serum Concentration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Modeled using a one-compartment pharmacokinetic model with first-order absorption and elimination (~14 day half-life).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (latestMed != null) "Medication: ${latestMed.name}" else "No Medication Selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (curvePoints.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Record injection history to generate PK curve.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val lineColor = MaterialTheme.colorScheme.primary
                        val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

                        val maxConc = (curvePoints.maxOfOrNull { it.concentration } ?: 1.0).coerceAtLeast(10.0)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                val paddingBottom = 30f
                                val chartHeight = height - paddingBottom

                                val stepX = width / (curvePoints.size - 1).coerceAtLeast(1)

                                val path = Path()
                                val fillPath = Path()

                                fillPath.moveTo(0f, chartHeight)

                                curvePoints.forEachIndexed { index, point ->
                                    val x = index * stepX
                                    val y = chartHeight - ((point.concentration / maxConc).toFloat() * chartHeight)
                                    if (index == 0) {
                                        path.moveTo(x, y)
                                        fillPath.lineTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                        fillPath.lineTo(x, y)
                                    }
                                }

                                fillPath.lineTo(width, chartHeight)
                                fillPath.close()

                                // Draw fill under curve
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(fillColor, Color.Transparent),
                                        startY = 0f,
                                        endY = chartHeight
                                    )
                                )

                                // Draw curve line
                                drawPath(
                                    path = path,
                                    color = lineColor,
                                    style = Stroke(width = 4f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "-30 Days",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "+30 Days",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // PK Modeling explanation card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "How BioShot PK Works",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "The curve aggregates all recorded dose events in your injection history. Each dose undergoes exponential absorption and elimination to model serum concentration trends over time. Use this to visualize accumulation and time-to-trough.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PkChartScreenPreview() {
    BioShotTheme {
        PkChartScreen(
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
                    timestamp = System.currentTimeMillis() - 86400000L * 14,
                    site = InjectionSite.ABDOMEN_LEFT,
                    dosageGiven = "40mg"
                )
            )
        )
    }
}
