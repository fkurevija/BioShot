package com.fkurevija.bioshot.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.core.model.ScheduleType
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val dosage: String,
    val frequency: ScheduleType,
    val customIntervalDays: Int = 14,
    val deviceType: DeviceType,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isInitialLoadingDose: Boolean = false,
    val loadingDoseAmount: String? = null,
    val inventoryCount: Int = 0,
    val expirationDate: Long? = null
)

fun MedicationEntity.getFormattedFrequency(): String {
    return when (frequency) {
        ScheduleType.DAILY -> "Daily"
        ScheduleType.WEEKLY -> "Weekly"
        ScheduleType.BIWEEKLY -> "Every 2 Weeks"
        ScheduleType.MONTHLY -> "Monthly"
        ScheduleType.AS_NEEDED -> "As Needed"
        ScheduleType.CUSTOM -> {
            val days = customIntervalDays.coerceAtLeast(1)
            if (days % 30 == 0) {
                val months = days / 30
                "Every $months ${if (months == 1) "Month" else "Months"}"
            } else if (days % 7 == 0) {
                val weeks = days / 7
                "Every $weeks ${if (weeks == 1) "Week" else "Weeks"}"
            } else {
                "Every $days ${if (days == 1) "Day" else "Days"}"
            }
        }
    }
}

@Serializable
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

@Serializable
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

@Serializable
@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val weight: Double,
    val unit: String = "lbs",
    val bodyFatPercentage: Double? = null,
    val waistCm: Double? = null,
    val notes: String? = null
)
