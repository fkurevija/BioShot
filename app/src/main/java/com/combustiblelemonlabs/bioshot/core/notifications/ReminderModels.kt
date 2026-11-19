package com.combustiblelemonlabs.bioshot.core.notifications

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class ReminderUnit(val labelSingular: String, val labelPlural: String) {
    MINUTES("minute", "minutes"),
    HOURS("hour", "hours"),
    DAYS("day", "days"),
    WEEKS("week", "weeks")
}

@Serializable
data class CustomReminder(
    val id: String = UUID.randomUUID().toString(),
    val amount: Int,
    val unit: ReminderUnit
) {
    val offsetMs: Long
        get() = when (unit) {
            ReminderUnit.MINUTES -> amount * 60 * 1000L
            ReminderUnit.HOURS -> amount * 3600 * 1000L
            ReminderUnit.DAYS -> amount * 86400 * 1000L
            ReminderUnit.WEEKS -> amount * 7 * 86400 * 1000L
        }

    fun getFormattedLabel(): String {
        if (amount == 0) return "At time of injection"
        val unitStr = if (amount == 1) unit.labelSingular else unit.labelPlural
        return "$amount $unitStr before"
    }

    fun getNotificationTextLabel(): String {
        if (amount == 0) return "is due now"
        val unitStr = if (amount == 1) unit.labelSingular else unit.labelPlural
        return when (unit) {
            ReminderUnit.DAYS -> if (amount == 1) "is due tomorrow" else "is due in $amount days"
            else -> "is due in $amount $unitStr"
        }
    }

    companion object {
        val DEFAULT_REMINDERS = listOf(
            CustomReminder(id = "default_1d", amount = 1, unit = ReminderUnit.DAYS),
            CustomReminder(id = "default_1w", amount = 1, unit = ReminderUnit.WEEKS)
        )
    }
}
