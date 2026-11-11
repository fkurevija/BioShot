package com.combustiblelemonlabs.bioshot.feature.settings

import com.combustiblelemonlabs.bioshot.core.database.InjectionLogEntity
import com.combustiblelemonlabs.bioshot.core.database.MedicationEntity
import com.combustiblelemonlabs.bioshot.core.database.SymptomEntryEntity
import com.combustiblelemonlabs.bioshot.core.model.DeviceType
import com.combustiblelemonlabs.bioshot.core.model.InjectionSite
import com.combustiblelemonlabs.bioshot.core.model.ScheduleType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FhirReportExporterTest {

    @Test
    fun buildFhirJsonString_createsValidFhirBundle() {
        val medications = listOf(
            MedicationEntity(
                id = 1L,
                name = "Humira",
                dosage = "40mg",
                frequency = ScheduleType.BIWEEKLY,
                deviceType = DeviceType.PEN
            )
        )

        val injectionLogs = listOf(
            InjectionLogEntity(
                id = 10L,
                medicationId = 1L,
                timestamp = 1700000000000L,
                site = InjectionSite.ABDOMEN_LEFT,
                dosageGiven = "40mg",
                lotNumber = "LOT123",
                painLevel = 2
            )
        )

        val symptomEntries = listOf(
            SymptomEntryEntity(
                id = 100L,
                timestamp = 1700000000000L,
                severity = 4,
                symptomName = "Joint Pain",
                notes = "Mild morning stiffness"
            )
        )

        val jsonString = FhirReportExporter.buildFhirJsonString(medications, injectionLogs, symptomEntries)
        assertNotNull(jsonString)

        val jsonObject = Json.parseToJsonElement(jsonString).jsonObject
        assertEquals("Bundle", jsonObject["resourceType"]?.jsonPrimitive?.content)
        assertEquals("collection", jsonObject["type"]?.jsonPrimitive?.content)

        val entries = jsonObject["entry"]?.jsonArray
        assertNotNull(entries)
        assertEquals(3, entries?.size)

        // Verify MedicationStatement
        val medEntry = entries?.get(0)?.jsonObject?.get("resource")?.jsonObject
        assertEquals("MedicationStatement", medEntry?.get("resourceType")?.jsonPrimitive?.content)
        assertEquals("med-1", medEntry?.get("id")?.jsonPrimitive?.content)

        // Verify MedicationAdministration
        val injEntry = entries?.get(1)?.jsonObject?.get("resource")?.jsonObject
        assertEquals("MedicationAdministration", injEntry?.get("resourceType")?.jsonPrimitive?.content)
        assertEquals("inj-10", injEntry?.get("id")?.jsonPrimitive?.content)

        // Verify Observation
        val symptomEntry = entries?.get(2)?.jsonObject?.get("resource")?.jsonObject
        assertEquals("Observation", symptomEntry?.get("resourceType")?.jsonPrimitive?.content)
        assertEquals("symptom-100", symptomEntry?.get("id")?.jsonPrimitive?.content)
    }

    @Test
    fun buildFhirJsonString_emptyLists() {
        val jsonString = FhirReportExporter.buildFhirJsonString(emptyList(), emptyList(), emptyList())
        assertNotNull(jsonString)

        val jsonObject = Json.parseToJsonElement(jsonString).jsonObject
        assertEquals("Bundle", jsonObject["resourceType"]?.jsonPrimitive?.content)
        val entries = jsonObject["entry"]?.jsonArray
        assertNotNull(entries)
        assertEquals(0, entries?.size)
    }
}
