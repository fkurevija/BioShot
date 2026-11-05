package com.combustiblelemonlabs.bioshot.core.schedule

import com.combustiblelemonlabs.bioshot.core.model.ScheduleType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ScheduleEngineTest {

    @Test
    fun testDailySchedule() {
        val baseDate = 1000000000L
        val res = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = baseDate,
            scheduleType = ScheduleType.DAILY
        )
        val cal = Calendar.getInstance().apply { timeInMillis = baseDate }
        cal.add(Calendar.DAY_OF_YEAR, 1)
        assertEquals(cal.timeInMillis, res.nextTimestamp)
    }

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

    @Test
    fun testAsNeededSchedule() {
        val baseDate = 1000000000L
        val res = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = baseDate,
            scheduleType = ScheduleType.AS_NEEDED
        )
        // As needed has 0 interval, adds 0 days
        assertEquals(baseDate, res.nextTimestamp)
    }

    @Test
    fun testGenerateUpcomingSchedules() {
        val baseDate = 1000000000L
        val dates = ScheduleEngine.generateUpcomingSchedules(
            baseDate = baseDate,
            scheduleType = ScheduleType.WEEKLY,
            count = 3
        )
        assertEquals(3, dates.size)
    }
}
