package com.combustiblelemonlabs.bioshot.feature.settings.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.combustiblelemonlabs.bioshot.core.database.InjectionLogEntity
import com.combustiblelemonlabs.bioshot.core.database.MedicationEntity
import com.combustiblelemonlabs.bioshot.core.database.SymptomEntryEntity
import com.combustiblelemonlabs.bioshot.feature.settings.FhirReportExporter
import com.combustiblelemonlabs.bioshot.feature.settings.PdfReportExporter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DoctorReportSection(
    medications: List<MedicationEntity>,
    injectionLogs: List<InjectionLogEntity>,
    symptomEntries: List<SymptomEntryEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val summaryText = remember(medications, injectionLogs, symptomEntries) {
        val sb = StringBuilder()
        sb.append("=== BIOSHOT THERAPY & SYMPTOM REPORT ===\n\n")
        sb.append("--- MEDICATIONS ---\n")
        medications.forEach { m ->
            sb.append("- ${m.name}: ${m.dosage}, Frequency: ${m.frequency.name}, Device: ${m.deviceType.name}\n")
        }
        sb.append("\n--- INJECTION HISTORY ---\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        injectionLogs.forEach { l ->
            val dateStr = dateFormat.format(Date(l.timestamp))
            sb.append("- Date: $dateStr | Site: ${l.site.name} | Dosage: ${l.dosageGiven} | Pain: ${l.painLevel ?: 0}/10 | Lot: ${l.lotNumber ?: "N/A"}\n")
        }
        sb.append("\n--- SYMPTOM DIARY ---\n")
        symptomEntries.forEach { s ->
            val dateStr = dateFormat.format(Date(s.timestamp))
            sb.append("- Date: $dateStr | Symptom: ${s.symptomName} | Severity: ${s.severity}/10 | Notes: ${s.notes ?: "None"}\n")
        }
        sb.toString()
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Doctor Visit Report Export",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Export your complete injection history, site rotation records, and symptom diary as a structured report for your rheumatologist or gastroenterologist.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Generate Shareable Report",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Includes ${medications.size} medications, ${injectionLogs.size} injections, and ${symptomEntries.size} symptom records.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = {
                        val pdfUri = PdfReportExporter.generatePdfReport(context, medications, injectionLogs, symptomEntries)
                        if (pdfUri != null) {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_SUBJECT, "BIOSHOT THERAPY & Symptom Report (PDF)")
                                putExtra(Intent.EXTRA_STREAM, pdfUri)
                                type = "application/pdf"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Clinical PDF Report")
                            context.startActivity(shareIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Clinical PDF Report (with PK Chart)")
                }

                OutlinedButton(
                    onClick = {
                        val fhirUri = FhirReportExporter.generateFhirJson(context, medications, injectionLogs, symptomEntries)
                        if (fhirUri != null) {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_SUBJECT, "BIOSHOT HL7 FHIR Clinical Export (JSON)")
                                putExtra(Intent.EXTRA_STREAM, fhirUri)
                                type = "application/json"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share HL7 FHIR Export")
                            context.startActivity(shareIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Rounded.Code, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export HL7 FHIR (JSON) for EHRs")
                }

                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_SUBJECT, "BIOSHOT THERAPY & Symptom Report")
                            putExtra(Intent.EXTRA_TEXT, summaryText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Rounded.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Plain Text Report")
                }
            }
        }
    }
}
