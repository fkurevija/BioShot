package com.fkurevija.bioshot.core.database

import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.ScheduleType
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationEntityTest {

    @Test
    fun testFormattedFrequencyDaily() {
        val med = MedicationEntity(name = "Test", dosage = "10mg", frequency = ScheduleType.DAILY, deviceType = DeviceType.PEN)
        assertEquals("Daily", med.getFormattedFrequency())
    }

    @Test
    fun testFormattedFrequencyWeekly() {
        val med = MedicationEntity(name = "Test", dosage = "40mg", frequency = ScheduleType.WEEKLY, deviceType = DeviceType.PEN)
        assertEquals("Weekly", med.getFormattedFrequency())
    }

    @Test
    fun testFormattedFrequencyBiweekly() {
        val med = MedicationEntity(name = "Test", dosage = "40mg", frequency = ScheduleType.BIWEEKLY, deviceType = DeviceType.PEN)
        assertEquals("Every 2 Weeks", med.getFormattedFrequency())
    }

    @Test
    fun testFormattedFrequencyMonthly() {
        val med = MedicationEntity(name = "Test", dosage = "40mg", frequency = ScheduleType.MONTHLY, deviceType = DeviceType.PEN)
        assertEquals("Monthly", med.getFormattedFrequency())
    }

    @Test
    fun testFormattedFrequencyCustomDays() {
        val med = MedicationEntity(name = "Test", dosage = "40mg", frequency = ScheduleType.CUSTOM, customIntervalDays = 10, deviceType = DeviceType.PEN)
        assertEquals("Every 10 Days", med.getFormattedFrequency())
    }
}
