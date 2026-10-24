package com.fkurevija.bioshot.feature.log.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.ui.theme.BioShotTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjectionLogBodyMap(
    selectedSite: InjectionSite,
    onSiteSelected: (InjectionSite) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Interactive Body Map",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Current Selected: ${selectedSite.name.replace("_", " ")}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        // Row 1: Abdomen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SiteButton(
                site = InjectionSite.ABDOMEN_LEFT,
                isSelected = selectedSite == InjectionSite.ABDOMEN_LEFT,
                onClick = { onSiteSelected(InjectionSite.ABDOMEN_LEFT) }
            )
            SiteButton(
                site = InjectionSite.ABDOMEN_RIGHT,
                isSelected = selectedSite == InjectionSite.ABDOMEN_RIGHT,
                onClick = { onSiteSelected(InjectionSite.ABDOMEN_RIGHT) }
            )
        }

        // Row 2: Thigh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SiteButton(
                site = InjectionSite.THIGH_LEFT,
                isSelected = selectedSite == InjectionSite.THIGH_LEFT,
                onClick = { onSiteSelected(InjectionSite.THIGH_LEFT) }
            )
            SiteButton(
                site = InjectionSite.THIGH_RIGHT,
                isSelected = selectedSite == InjectionSite.THIGH_RIGHT,
                onClick = { onSiteSelected(InjectionSite.THIGH_RIGHT) }
            )
        }

        // Row 3: Arm
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SiteButton(
                site = InjectionSite.ARM_LEFT,
                isSelected = selectedSite == InjectionSite.ARM_LEFT,
                onClick = { onSiteSelected(InjectionSite.ARM_LEFT) }
            )
            SiteButton(
                site = InjectionSite.ARM_RIGHT,
                isSelected = selectedSite == InjectionSite.ARM_RIGHT,
                onClick = { onSiteSelected(InjectionSite.ARM_RIGHT) }
            )
        }

        // Row 4: Buttock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SiteButton(
                site = InjectionSite.BUTTOCK_LEFT,
                isSelected = selectedSite == InjectionSite.BUTTOCK_LEFT,
                onClick = { onSiteSelected(InjectionSite.BUTTOCK_LEFT) }
            )
            SiteButton(
                site = InjectionSite.BUTTOCK_RIGHT,
                isSelected = selectedSite == InjectionSite.BUTTOCK_RIGHT,
                onClick = { onSiteSelected(InjectionSite.BUTTOCK_RIGHT) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SiteButton(
    site: InjectionSite,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val label = site.name.replace("_", " ")
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}