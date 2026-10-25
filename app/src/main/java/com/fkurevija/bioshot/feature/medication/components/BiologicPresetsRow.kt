package com.fkurevija.bioshot.feature.medication.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.model.ScheduleType
import com.fkurevija.bioshot.feature.medication.BIOLOGIC_PRESETS
import com.fkurevija.bioshot.feature.medication.CustomUnit
import com.fkurevija.bioshot.feature.medication.FrequencyPreset

@Composable
fun BiologicPresetsRow(
    selectedName: String,
    onSelectPreset: (String, String, FrequencyPreset, String, CustomUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                val isSelected = selectedName == preset.name
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newPreset = when (preset.scheduleType) {
                            ScheduleType.DAILY -> FrequencyPreset.DAILY
                            ScheduleType.WEEKLY -> FrequencyPreset.WEEKLY
                            ScheduleType.BIWEEKLY -> FrequencyPreset.BIWEEKLY
                            ScheduleType.MONTHLY -> FrequencyPreset.MONTHLY
                            ScheduleType.AS_NEEDED -> FrequencyPreset.AS_NEEDED
                            ScheduleType.CUSTOM -> FrequencyPreset.CUSTOM
                        }
                        var numText = "4"
                        var unit = CustomUnit.WEEKS
                        if (preset.scheduleType == ScheduleType.CUSTOM) {
                            if (preset.defaultDays % 30 == 0) {
                                numText = (preset.defaultDays / 30).toString()
                                unit = CustomUnit.MONTHS
                            } else if (preset.defaultDays % 7 == 0) {
                                numText = (preset.defaultDays / 7).toString()
                                unit = CustomUnit.WEEKS
                            } else {
                                numText = preset.defaultDays.toString()
                                unit = CustomUnit.DAYS
                            }
                        }
                        onSelectPreset(preset.name, preset.defaultDosage, newPreset, numText, unit)
                    },
                    label = { Text(preset.name) }
                )
            }
        }
    }
}
