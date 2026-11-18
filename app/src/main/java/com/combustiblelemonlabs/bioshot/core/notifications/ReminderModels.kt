package com.combustiblelemonlabs.bioshot.core.notifications

enum class ReminderMode {
    OFF,
    SINGLE,
    MULTIPLE
}

enum class ReminderInterval(
    val label: String,
    val offsetDays: Long,
    val reminderLabel: String,
    val defaultForSingle: Boolean = false
) {
    ONE_WEEK_BEFORE(
        label = "1 week before",
        offsetDays = 7L,
        reminderLabel = "is due in 1 week"
    ),
    THREE_DAYS_BEFORE(
        label = "3 days before",
        offsetDays = 3L,
        reminderLabel = "is due in 3 days"
    ),
    ONE_DAY_BEFORE(
        label = "1 day before",
        offsetDays = 1L,
        reminderLabel = "is due tomorrow",
        defaultForSingle = true
    ),
    AT_TIME_OF_INJECTION(
        label = "At time of injection",
        offsetDays = 0L,
        reminderLabel = "is due today"
    );

    companion object {
        fun fromName(name: String): ReminderInterval? = entries.find { it.name == name }
    }
}
