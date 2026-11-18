package com.combustiblelemonlabs.bioshot.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.combustiblelemonlabs.bioshot.MainActivity

class InjectionReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val medicationName = inputData.getString("medicationName") ?: "Biologic Medication"
        val dosage = inputData.getString("dosage") ?: ""
        val reminderLabel = inputData.getString("reminderLabel") ?: "is due today"
        val notificationId = inputData.getInt("notificationId", 1001)

        showNotification(medicationName, dosage, reminderLabel, notificationId)
        return Result.success()
    }

    private fun showNotification(medicationName: String, dosage: String, reminderLabel: String, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bioshot_dose_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Dose Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled biologic medication injections"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (dosage.isNotBlank()) {
            "Your scheduled injection for $medicationName ($dosage) $reminderLabel."
        } else {
            "Your scheduled injection for $medicationName $reminderLabel."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("BioShot Injection Reminder")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
