package com.combustiblelemonlabs.bioshot.core.schedule

import com.combustiblelemonlabs.bioshot.core.model.ScheduleType
import java.util.Calendar
import java.util.Date

/**
 * ScheduleEngine computes next injection dates supporting standard intervals (weekly, biweekly, monthly,
 * daily, custom intervals) and loading-dose schedules (e.g., specific weeks 0, 1, 2 then maintenance).
 */
object ScheduleEngine {

    /**
     * Calculates the next scheduled injection date given the last injection date, schedule type,
     * optional custom interval in days, loading dose steps, and current sequence index.
     *
     * @param lastInjectionDate The timestamp (milliseconds) of the last injection.
     * @param scheduleType The base [ScheduleType].
     * @param customIntervalDays Used if scheduleType is [ScheduleType.CUSTOM].
     * @param loadingDoseIntervalsDays Optional list of loading dose intervals in days (e.g., [7, 7, 14] for weeks 0, 1, 2, then maintenance).
     * @param currentLoadingDoseIndex The current index in the loading dose intervals list. If null or out of bounds, standard interval is used.
     * @return A Result object containing the next injection timestamp (milliseconds) and the next loading dose index (if applicable).
     */
    data class ScheduleResult(
        val nextTimestamp: Long,
        val nextLoadingDoseIndex: Int? = null,
        val isMaintenance: Boolean = false
    )

    fun calculateNextInjection(
        lastInjectionDate: Long,
        scheduleType: ScheduleType,
        customIntervalDays: Int = 7,
        loadingDoseIntervalsDays: List<Int>? = null,
        currentLoadingDoseIndex: Int? = null
    ): ScheduleResult {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = lastInjectionDate
        }

        // 1. Check Loading Dose Schedule if provided
        if (!loadingDoseIntervalsDays.isNullOrEmpty()) {
            val currentIndex = currentLoadingDoseIndex ?: 0
            if (currentIndex < loadingDoseIntervalsDays.size) {
                val intervalDays = loadingDoseIntervalsDays[currentIndex]
                calendar.add(Calendar.DAY_OF_YEAR, intervalDays)
                val nextIndex = currentIndex + 1
                val isMaint = nextIndex >= loadingDoseIntervalsDays.size
                return ScheduleResult(
                    nextTimestamp = calendar.timeInMillis,
                    nextLoadingDoseIndex = if (isMaint) null else nextIndex,
                    isMaintenance = isMaint
                )
            }
        }

        // 2. Standard Schedule Intervals
        val intervalDays = when (scheduleType) {
            ScheduleType.DAILY -> 1
            ScheduleType.WEEKLY -> 7
            ScheduleType.BIWEEKLY -> 14
            ScheduleType.MONTHLY -> 30 // Approximate or Calendar month
            ScheduleType.AS_NEEDED -> 0 // Manual / As Needed has no automatic next date unless specified
            ScheduleType.CUSTOM -> customIntervalDays.coerceAtLeast(1)
        }

        if (intervalDays > 0) {
            if (scheduleType == ScheduleType.MONTHLY) {
                calendar.add(Calendar.MONTH, 1)
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, intervalDays)
            }
        }

        return ScheduleResult(
            nextTimestamp = calendar.timeInMillis,
            nextLoadingDoseIndex = null,
            isMaintenance = true
        )
    }

    /**
     * Generates a series of upcoming injection dates starting from a base date.
     */
    fun generateUpcomingSchedules(
        baseDate: Long,
        scheduleType: ScheduleType,
        count: Int,
        customIntervalDays: Int = 7,
        loadingDoseIntervalsDays: List<Int>? = null
    ): List<Long> {
        val results = mutableListOf<Long>()
        var currentDate = baseDate
        var loadingIndex: Int? = if (!loadingDoseIntervalsDays.isNullOrEmpty()) 0 else null

        for (i in 0 until count) {
            val res = calculateNextInjection(
                lastInjectionDate = currentDate,
                scheduleType = scheduleType,
                customIntervalDays = customIntervalDays,
                loadingDoseIntervalsDays = loadingDoseIntervalsDays,
                currentLoadingDoseIndex = loadingIndex
            )
            currentDate = res.nextTimestamp
            results.add(currentDate)
            loadingIndex = res.nextLoadingDoseIndex
        }

        return results
    }
}
