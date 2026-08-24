package com.example.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.TimelineBlockEntity
import java.util.*

class TimelineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Rudra Life OS"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Time for your scheduled block."
        val notificationId = intent.getIntExtra("EXTRA_ID", 1001)

        TimelineNotificationHelper.showNotification(context, notificationId, title, message)
    }
}

object TimelineNotificationHelper {
    const val CHANNEL_ID = "rudra_timeline_channel"
    const val CHANNEL_NAME = "Rudra Timeline & Routine Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies before and at scheduled routine blocks"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, id: Int, title: String, message: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notification)
    }

    fun scheduleBlockReminders(context: Context, blocks: List<TimelineBlockEntity>) {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        blocks.forEach { block ->
            val parts = block.startTime.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: return@forEach
                val minute = parts[1].toIntOrNull() ?: return@forEach

                // 15 min before
                scheduleSingleAlarm(
                    context,
                    alarmManager,
                    block.id.toInt() * 100 + 1,
                    hour,
                    minute - 15,
                    "Upcoming: ${block.title} in 15 mins",
                    "Trigger: ${block.triggerAction}"
                )

                // 5 min before
                scheduleSingleAlarm(
                    context,
                    alarmManager,
                    block.id.toInt() * 100 + 2,
                    hour,
                    minute - 5,
                    "Ready: ${block.title} in 5 mins",
                    "Prepare: ${block.subtitle}"
                )

                // At start time
                scheduleSingleAlarm(
                    context,
                    alarmManager,
                    block.id.toInt() * 100 + 3,
                    hour,
                    minute,
                    "Active Now: ${block.title}",
                    block.triggerAction.ifBlank { block.subtitle }
                )
            }
        }
    }

    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        targetHour: Int,
        targetMinute: Int,
        title: String,
        message: String
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1) // next occurrence tomorrow
            }
        }

        val intent = Intent(context, TimelineAlarmReceiver::class.java).apply {
            putExtra("EXTRA_ID", requestCode)
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_MESSAGE", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            // If exact alarm permission is missing on Android 12+, fallback to inexact
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }
}
