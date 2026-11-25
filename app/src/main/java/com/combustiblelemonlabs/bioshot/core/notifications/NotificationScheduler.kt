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
        remindersEnabled: Boolean,
        reminders: List<CustomReminder>,
        preferredHour: Int = -1,
        preferredMinute: Int = -1,
    ) {
        cancelReminders(context)

        if (!remindersEnabled || reminders.isEmpty()) return

        reminders.forEachIndexed { index, reminder ->
            val targetCalendar = Calendar.getInstance(TimeZone.getDefault()).apply {
                timeInMillis = nextDoseTimeMs - reminder.offsetMs
                if (((reminder.unit == ReminderUnit.DAYS) || (reminder.unit == ReminderUnit.WEEKS)) && (preferredHour >= 0) && (preferredMinute >= 0)) {
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
                    .putString("reminderLabel", reminder.getNotificationTextLabel())
                    .putInt("notificationId", 1000 + index)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<InjectionReminderWorker>()
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build()

                val uniqueName = "$WORK_PREFIX${reminder.id}"
                WorkManager.getInstance(context).enqueueUniqueWork(
                    uniqueName,
                    ExistingWorkPolicy.REPLACE,
                    workRequest,
                )
            }
        }
    }

    fun cancelReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(LEGACY_WORK_NAME)
        workManager.cancelAllWorkByTag(WORK_PREFIX)
    }
}
