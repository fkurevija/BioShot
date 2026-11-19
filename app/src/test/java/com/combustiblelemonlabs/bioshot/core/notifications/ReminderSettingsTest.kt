package com.combustiblelemonlabs.bioshot.core.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderSettingsTest {

    @Test
    fun customReminder_offsetsCalculatedCorrectly() {
        val tenMins = CustomReminder(amount = 10, unit = ReminderUnit.MINUTES)
        assertEquals(10 * 60 * 1000L, tenMins.offsetMs)

        val oneHour = CustomReminder(amount = 1, unit = ReminderUnit.HOURS)
        assertEquals(3600 * 1000L, oneHour.offsetMs)

        val oneDay = CustomReminder(amount = 1, unit = ReminderUnit.DAYS)
        assertEquals(86400 * 1000L, oneDay.offsetMs)

        val oneWeek = CustomReminder(amount = 1, unit = ReminderUnit.WEEKS)
        assertEquals(7 * 86400 * 1000L, oneWeek.offsetMs)
    }

    @Test
    fun customReminder_formattedLabels() {
        val tenMins = CustomReminder(amount = 10, unit = ReminderUnit.MINUTES)
        assertEquals("10 minutes before", tenMins.getFormattedLabel())
        assertEquals("is due in 10 minutes", tenMins.getNotificationTextLabel())

        val oneHour = CustomReminder(amount = 1, unit = ReminderUnit.HOURS)
        assertEquals("1 hour before", oneHour.getFormattedLabel())
        assertEquals("is due in 1 hour", oneHour.getNotificationTextLabel())

        val oneDay = CustomReminder(amount = 1, unit = ReminderUnit.DAYS)
        assertEquals("1 day before", oneDay.getFormattedLabel())
        assertEquals("is due tomorrow", oneDay.getNotificationTextLabel())

        val zero = CustomReminder(amount = 0, unit = ReminderUnit.MINUTES)
        assertEquals("At time of injection", zero.getFormattedLabel())
        assertEquals("is due now", zero.getNotificationTextLabel())
    }
}
