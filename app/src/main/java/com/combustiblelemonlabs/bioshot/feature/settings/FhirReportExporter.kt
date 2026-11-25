package com.combustiblelemonlabs.bioshot.feature.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.combustiblelemonlabs.bioshot.core.database.InjectionLogEntity
import com.combustiblelemonlabs.bioshot.core.database.MedicationEntity
import com.combustiblelemonlabs.bioshot.core.database.SymptomEntryEntity
import kotlinx.serialization.json.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FhirReportExporter {

    private val jsonFormat = Json { prettyPrint = true }

    fun buildFhirJsonString(
        medications: List<MedicationEntity>,
        injectionLogs: List<InjectionLogEntity>,
        symptomEntries: List<SymptomEntryEntity>,
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val entries = mutableListOf<JsonObject>()

        // Add medications as MedicationStatement resources
        medications.forEach { (id, name, dosage, frequency, _, deviceType) ->
            entries.add(
                buildJsonObject {
                    put("resourceType", "MedicationStatement")
                    put("id", "med-$id")
                    put("status", "active")
                    put(
                        "medicationCodeableConcept",
                        buildJsonObject {
                            put("text", name)
                        },
                    )
                    put(
                        "dosage",
                        buildJsonArray {
                            add(
                                buildJsonObject {
                                    put("text", "$dosage (${frequency.name}, ${deviceType.name})")
                                },
                            )
                        },
                    )
                },
            )
        }

        // Add injection logs as MedicationAdministration resources
        injectionLogs.forEach { (id, _, timestamp, site, dosageGiven, lotNumber, _, painLevel) ->
            val dateStr = dateFormat.format(Date(timestamp))
            entries.add(
                buildJsonObject {
                    put("resourceType", "MedicationAdministration")
                    put("id", "inj-$id")
                    put("status", "completed")
                    put("effectiveDateTime", dateStr)
                    put(
                        "dosage",
                        buildJsonObject {
                            put("text", dosageGiven)
                        },
                    )
                    put(
                        "site",
                        buildJsonObject {
                            put("text", site.name)
                        },
                    )
                    if (!lotNumber.isNullOrBlank()) {
                        put(
                            "note",
                            buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put("text", "Lot: $lotNumber, Pain: ${painLevel ?: 0}/10")
                                    },
                                )
                            },
                        )
                    }
                },
            )
        }

        // Add symptom entries as Observation resources
        symptomEntries.forEach { (id, timestamp, severity, symptomName, notes) ->
            val dateStr = dateFormat.format(Date(timestamp))
            entries.add(
                buildJsonObject {
                    put("resourceType", "Observation")
                    put("id", "symptom-$id")
                    put("status", "final")
                    put(
                        "code",
                        buildJsonObject {
                            put("text", symptomName)
                        },
                    )
                    put("effectiveDateTime", dateStr)
                    put("valueInteger", severity)
                    if (!notes.isNullOrBlank()) {
                        put(
                            "note",
                            buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put("text", notes)
                                    },
                                )
                            },
                        )
                    }
                },
            )
        }

        val fhirBundle = buildJsonObject {
            put("resourceType", "Bundle")
            put("type", "collection")
            put("timestamp", dateFormat.format(Date()))
            put(
                "entry",
                buildJsonArray {
                    entries.forEach { add(JsonObject(mapOf("resource" to it))) }
                },
            )
        }

        return jsonFormat.encodeToString(JsonObject.serializer(), fhirBundle)
    }

    fun generateFhirJson(
        context: Context,
        medications: List<MedicationEntity>,
        injectionLogs: List<InjectionLogEntity>,
        symptomEntries: List<SymptomEntryEntity>,
    ): Uri? {
        val jsonString = buildFhirJsonString(medications, injectionLogs, symptomEntries)
        val file = File(context.cacheDir, "BioShot_FHIR_Clinical_Export.json")
        file.writeText(jsonString)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }
}
