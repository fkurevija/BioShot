package com.fkurevija.bioshot.core.schedule

import com.fkurevija.bioshot.core.model.ScheduleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ScheduleEngineTest {

    @Test
    fun testWeeklySchedule() {
        val baseDate = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 1, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val result = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = baseDate,
            scheduleType = ScheduleType.WEEKLY
        )

        val expected = Calendar.getInstance().apply {
            timeInMillis = baseDate
            add(Calendar.DAY_OF_YEAR, 7)
        }.timeInMillis

        assertEquals(expected, result.nextTimestamp)
        assertTrue(result.isMaintenance)
    }

    @Test
    fun testBiweeklySchedule() {
        val baseDate = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 1, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val result = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = baseDate,
            scheduleType = ScheduleType.BIWEEKLY
        )

        val expected = Calendar.getInstance().apply {
            timeInMillis = baseDate
            add(Calendar.DAY_OF_YEAR, 14)
        }.timeInMillis

        assertEquals(expected, result.nextTimestamp)
    }

    @Test
    fun testLoadingDoseScheduleSequence() {
        val baseDate = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 1, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Loading dose intervals: e.g. [7, 7, 14] (Weeks 0 -> 1 -> 2 -> Maintenance)
        val loadingIntervals = listOf(7, 7, 14)

        // Step 1: index 0
        val res1 = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = baseDate,
            scheduleType = ScheduleType.WEEKLY,
            loadingDoseIntervalsDays = loadingIntervals,
            currentLoadingDoseIndex = 0
        )
        assertEquals(1, res1.nextLoadingDoseIndex)
        assertEquals(false, res1.isMaintenance)

        // Step 2: index 1
        val res2 = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = res1.nextTimestamp,
            scheduleType = ScheduleType.WEEKLY,
            loadingDoseIntervalsDays = loadingIntervals,
            currentLoadingDoseIndex = res1.nextLoadingDoseIndex
        )
        assertEquals(2, res2.nextLoadingDoseIndex)
        assertEquals(false, res2.isMaintenance)

        // Step 3: index 2 (last loading dose interval, moves to maintenance)
        val res3 = ScheduleEngine.calculateNextInjection(
            lastInjectionDate = res2.nextTimestamp,
            scheduleType = ScheduleType.WEEKLY,
            loadingDoseIntervalsDays = loadingIntervals,
            currentLoadingDoseIndex = res2.nextLoadingDoseIndex
        )
        assertEquals(null, res3.nextLoadingDoseIndex)
        assertEquals(true, res3.isMaintenance)
    }

    @Test
    fun testGenerateUpcomingSchedules() {
        val baseDate = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 1, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val upcoming = ScheduleEngine.generateUpcomingSchedules(
            baseDate = baseDate,
            scheduleType = ScheduleType.WEEKLY,
            count = 3
        )

        assertEquals(3, upcoming.size)
    }
}
