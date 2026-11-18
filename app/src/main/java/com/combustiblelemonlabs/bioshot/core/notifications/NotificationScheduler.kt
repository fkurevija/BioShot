package com.combustiblelemonlabs.bioshot.core.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val LEGACY_WORK_NAME = "bioshot_injection_reminder_work"
    private const val WORK_PREFIX = "bioshot_reminder_"

    fun scheduleReminders(
        context: Context,
        nextDoseTimeMs: Long,
        medicationName: String,
        dosage: String,
        reminderMode: ReminderMode,
        reminderIntervals: Set<ReminderInterval>,
        preferredHour: Int = -1,
        preferredMinute: Int = -1
    ) {
        cancelReminders(context)

        if (reminderMode == ReminderMode.OFF) return

        val activeIntervals = when (reminderMode) {
            ReminderMode.SINGLE -> {
                val single = reminderIntervals.firstOrNull() ?: ReminderInterval.ONE_DAY_BEFORE
                setOf(single)
            }
            ReminderMode.MULTIPLE -> {
                if (reminderIntervals.isEmpty()) {
                    setOf(ReminderInterval.ONE_DAY_BEFORE, ReminderInterval.AT_TIME_OF_INJECTION)
                } else {
                    reminderIntervals
                }
            }
            ReminderMode.OFF -> emptySet()
        }

        activeIntervals.forEach { interval ->
            val baseTimestamp = nextDoseTimeMs - (interval.offsetDays * 24 * 60 * 60 * 1000L)
            val targetCalendar = Calendar.getInstance(TimeZone.getDefault()).apply {
                timeInMillis = baseTimestamp
                if (preferredHour >= 0 && preferredMinute >= 0) {
                    set(Calendar.HOUR_OF_DAY, preferredHour)
                    set(Calendar.MINUTE, preferredMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }

            val delayMs = targetCalendar.timeInMillis - System.currentTimeMillis()
            if (delayMs > 0) {
                val inputData = Data.Builder()
                    .putString("medicationName", medicationName)
                    .putString("dosage", dosage)
                    .putString("reminderLabel", interval.reminderLabel)
                    .putInt("notificationId", 1000 + interval.ordinal)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<InjectionReminderWorker>()
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build()

                val uniqueName = "$WORK_PREFIX${interval.name}"
                WorkManager.getInstance(context).enqueueUniqueWork(
                    uniqueName,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
    }

    fun scheduleReminder(
        context: Context,
        nextDoseTimeMs: Long,
        medicationName: String,
        dosage: String,
        preferredHour: Int = -1,
        preferredMinute: Int = -1
    ) {
        scheduleReminders(
            context = context,
            nextDoseTimeMs = nextDoseTimeMs,
            medicationName = medicationName,
            dosage = dosage,
            reminderMode = ReminderMode.SINGLE,
            reminderIntervals = setOf(ReminderInterval.ONE_DAY_BEFORE),
            preferredHour = preferredHour,
            preferredMinute = preferredMinute
        )
    }

    fun cancelReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(LEGACY_WORK_NAME)
        ReminderInterval.entries.forEach { interval ->
            workManager.cancelUniqueWork("$WORK_PREFIX${interval.name}")
        }
    }

    fun cancelReminder(context: Context) {
        cancelReminders(context)
    }
}
