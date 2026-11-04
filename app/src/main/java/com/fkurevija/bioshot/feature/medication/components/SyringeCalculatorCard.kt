package com.fkurevija.bioshot.feature.medication.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SyringeCalculatorCard(
    onApplyDosage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var vialMgText by remember { mutableStateOf("10") }
    var bacWaterMlText by remember { mutableStateOf("2") }
    var desiredMgText by remember { mutableStateOf("2.5") }

    val currentLocale = LocalLocale.current.platformLocale

    val vialMg = vialMgText.toDoubleOrNull() ?: 0.0
    val bacWaterMl = bacWaterMlText.toDoubleOrNull() ?: 0.0
    val desiredMg = desiredMgText.toDoubleOrNull() ?: 0.0

    val calculatedUnits = if (vialMg > 0.0 && bacWaterMl > 0.0) {
        (desiredMg / vialMg) * bacWaterMl * 100.0
    } else {
        0.0
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "GLP-1 / Peptide Syringe Calculator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null
                )
            }

            if (expanded) {
                Text(
                    text = "Convert vial milligrams and bacteriostatic water into U-100 insulin syringe units.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = vialMgText,
                        onValueChange = { vialMgText = it },
                        label = { Text("Vial (mg)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bacWaterMlText,
                        onValueChange = { bacWaterMlText = it },
                        label = { Text("Water (mL)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = desiredMgText,
                        onValueChange = { desiredMgText = it },
                        label = { Text("Dose (mg)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Calculated Syringe Draw:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = String.format(currentLocale, "%.1f Units on U-100 Syringe", calculatedUnits),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Button(
                    onClick = {
                        onApplyDosage("${desiredMgText}mg (${String.format(currentLocale, "%.1f", calculatedUnits)} units)")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply to Dosage Field")
                }
            }
        }
    }
}
