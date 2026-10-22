package com.fkurevija.bioshot.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.core.model.ScheduleType

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val dosage: String,
    val frequency: ScheduleType,
    val deviceType: DeviceType,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "injection_logs")
data class InjectionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val medicationId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val site: InjectionSite,
    val dosageGiven: String,
    val lotNumber: String? = null,
    val notes: String? = null,
    val painLevel: Int? = null,
    val sideEffects: List<String> = emptyList()
)

@Entity(tableName = "symptom_entries")
data class SymptomEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val severity: Int,
    val symptomName: String,
    val notes: String? = null,
    val tags: List<String> = emptyList()
)
