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

    private const val WORK_NAME = "bioshot_injection_reminder_work"

    fun scheduleReminder(
        context: Context,
        nextDoseTimeMs: Long,
        medicationName: String,
        dosage: String,
        preferredHour: Int = -1,
        preferredMinute: Int = -1
    ) {
        val targetCalendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = nextDoseTimeMs
            if (preferredHour >= 0 && preferredMinute >= 0) {
                set(Calendar.HOUR_OF_DAY, preferredHour)
                set(Calendar.MINUTE, preferredMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }

        val delayMs = targetCalendar.timeInMillis - System.currentTimeMillis()
        if (delayMs <= 0) return

        val inputData = Data.Builder()
            .putString("medicationName", medicationName)
            .putString("dosage", dosage)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<InjectionReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
