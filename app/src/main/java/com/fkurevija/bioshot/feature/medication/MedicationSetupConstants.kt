package com.fkurevija.bioshot.feature.medication

import com.fkurevija.bioshot.core.model.ScheduleType

data class BiologicPreset(val name: String, val defaultDosage: String, val scheduleType: ScheduleType, val defaultDays: Int)

val BIOLOGIC_PRESETS = listOf(
    BiologicPreset("Humira (Adalimumab)", "40mg", ScheduleType.BIWEEKLY, 14),
    BiologicPreset("Enbrel (Etanercept)", "50mg", ScheduleType.WEEKLY, 7),
    BiologicPreset("Stelara (Ustekinumab)", "45mg", ScheduleType.CUSTOM, 84), // Every 12 weeks
    BiologicPreset("Cosentyx (Secukinumab)", "300mg", ScheduleType.WEEKLY, 7),
    BiologicPreset("Dupixent (Dupilumab)", "300mg", ScheduleType.BIWEEKLY, 14),
    BiologicPreset("Simponi (Golimumab)", "50mg", ScheduleType.CUSTOM, 28), // Every 4 weeks
)

val GLP1_PRESETS = listOf(
    BiologicPreset("Mounjaro / Zepbound (Tirzepatide)", "2.5mg", ScheduleType.WEEKLY, 7),
    BiologicPreset("Wegovy / Ozempic (Semaglutide)", "0.25mg", ScheduleType.WEEKLY, 7),
    BiologicPreset("Retatrutide", "2mg", ScheduleType.WEEKLY, 7),
    BiologicPreset("Saxenda (Liraglutide)", "0.6mg", ScheduleType.DAILY, 1)
)

enum class FrequencyPreset(val label: String, val scheduleType: ScheduleType, val defaultDays: Int) {
    DAILY("Daily", ScheduleType.DAILY, 1),
    WEEKLY("Weekly (Every 7 days)", ScheduleType.WEEKLY, 7),
    BIWEEKLY("Every 2 Weeks (Biweekly)", ScheduleType.BIWEEKLY, 14),
    MONTHLY("Monthly", ScheduleType.MONTHLY, 30),
    AS_NEEDED("As Needed / Manual", ScheduleType.AS_NEEDED, 0),
    CUSTOM("Custom...", ScheduleType.CUSTOM, 28)
}

enum class CustomUnit(val label: String, val daysMultiplier: Int) {
    DAYS("Days", 1),
    WEEKS("Weeks", 7),
    MONTHS("Months", 30)
}
