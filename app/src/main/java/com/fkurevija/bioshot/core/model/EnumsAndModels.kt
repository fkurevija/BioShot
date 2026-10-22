package com.fkurevija.bioshot.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class InjectionSite {
    ABDOMEN_LEFT,
    ABDOMEN_RIGHT,
    THIGH_LEFT,
    THIGH_RIGHT,
    ARM_LEFT,
    ARM_RIGHT,
    BUTTOCK_LEFT,
    BUTTOCK_RIGHT
}

@Serializable
enum class DeviceType {
    AUTO_INJECTOR,
    PEN,
    SYRINGE,
    PUMP
}

@Serializable
enum class ScheduleType {
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    DAILY,
    AS_NEEDED,
    CUSTOM
}

@Serializable
data class Medication(
    val id: Long = 0L,
    val name: String,
    val dosage: String,
    val frequency: ScheduleType,
    val deviceType: DeviceType,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class InjectionLog(
    val id: Long = 0L,
    val medicationId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val site: InjectionSite,
    val dosageGiven: String,
    val lotNumber: String? = null,
    val notes: String? = null,
    val painLevel: Int? = null, // 0 - 10
    val sideEffects: List<String> = emptyList()
)

@Serializable
data class SymptomEntry(
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val severity: Int, // 1 - 10
    val symptomName: String,
    val notes: String? = null,
    val tags: List<String> = emptyList()
)
