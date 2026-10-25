package com.fkurevija.bioshot.core.schedule

import com.fkurevija.bioshot.core.model.ScheduleType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ScheduleEngineTest {

    @Test
    fun testWeeklySchedule() {
        val baseDate = 1000000000L
        val res = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = baseDate,
            scheduleType = ScheduleType.WEEKLY
        )
        val cal = Calendar.getInstance().apply { timeInMillis = baseDate }
        cal.add(Calendar.DAY_OF_YEAR, 7)
        assertEquals(cal.timeInMillis, res.nextTimestamp)
    }

    @Test
    fun testBiweeklySchedule() {
        val baseDate = 1000000000L
        val res = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = baseDate,
            scheduleType = ScheduleType.BIWEEKLY
        )
        val cal = Calendar.getInstance().apply { timeInMillis = baseDate }
        cal.add(Calendar.DAY_OF_YEAR, 14)
        assertEquals(cal.timeInMillis, res.nextTimestamp)
    }

    @Test
    fun testMonthlySchedule() {
        val baseDate = 1000000000L
        val res = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = baseDate,
            scheduleType = ScheduleType.MONTHLY
        )
        val cal = Calendar.getInstance().apply { timeInMillis = baseDate }
        cal.add(Calendar.MONTH, 1)
        assertEquals(cal.timeInMillis, res.nextTimestamp)
    }

    @Test
    fun testCustomSchedule() {
        val baseDate = 1000000000L
        val res = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = baseDate,
            scheduleType = ScheduleType.CUSTOM,
            customIntervalDays = 10
        )
        val cal = Calendar.getInstance().apply { timeInMillis = baseDate }
        cal.add(Calendar.DAY_OF_YEAR, 10)
        assertEquals(cal.timeInMillis, res.nextTimestamp)
    }
}
