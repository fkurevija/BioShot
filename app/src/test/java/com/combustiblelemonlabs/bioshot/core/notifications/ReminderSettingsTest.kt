package com.combustiblelemonlabs.bioshot.core.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSettingsTest {

    @Test
    fun reminderInterval_propertiesMatchOffset() {
        assertEquals(7L, ReminderInterval.ONE_WEEK_BEFORE.offsetDays)
        assertEquals(3L, ReminderInterval.THREE_DAYS_BEFORE.offsetDays)
        assertEquals(1L, ReminderInterval.ONE_DAY_BEFORE.offsetDays)
        assertEquals(0L, ReminderInterval.AT_TIME_OF_INJECTION.offsetDays)

        assertEquals("is due in 1 week", ReminderInterval.ONE_WEEK_BEFORE.reminderLabel)
        assertEquals("is due in 3 days", ReminderInterval.THREE_DAYS_BEFORE.reminderLabel)
        assertEquals("is due tomorrow", ReminderInterval.ONE_DAY_BEFORE.reminderLabel)
        assertEquals("is due today", ReminderInterval.AT_TIME_OF_INJECTION.reminderLabel)
    }

    @Test
    fun reminderInterval_fromName() {
        assertEquals(ReminderInterval.ONE_WEEK_BEFORE, ReminderInterval.fromName("ONE_WEEK_BEFORE"))
        assertEquals(ReminderInterval.AT_TIME_OF_INJECTION, ReminderInterval.fromName("AT_TIME_OF_INJECTION"))
        assertEquals(null, ReminderInterval.fromName("UNKNOWN_INTERVAL"))
    }
}
