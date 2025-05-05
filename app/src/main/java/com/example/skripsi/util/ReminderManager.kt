package com.example.skripsi.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

class ReminderManager(private val context: Context) {

    companion object {
        const val ACTION_READING_REMINDER = "com.example.skripsi.ACTION_READING_REMINDER"
        private const val REMINDER_REQUEST_CODE = 2001
    }


    fun scheduleDailyReminder(testing: Boolean = false) {
        val preferenceManager = PreferenceManager(context)

        if (!preferenceManager.isDailyReminderEnabled()) {
            Log.d("ReminderManager", "Daily reading reminders are disabled in preferences")
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_READING_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()

        if (testing) {
            calendar.timeInMillis = System.currentTimeMillis() + 30 * 1000
            Log.d("ReminderManager", "TEST MODE: Scheduling reminder for 30 seconds from now")
        } else {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, 17)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d("ReminderManager", "Scheduling reminder for: ${calendar.time}")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !testing) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("ReminderManager", "Scheduled exact alarm with ALLOW_WHILE_IDLE")
                } else {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                    Log.d("ReminderManager", "Scheduled inexact repeating alarm (no permission for exact)")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (testing) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("ReminderManager", "Scheduled one-time test alarm")
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("ReminderManager", "Scheduled exact alarm with ALLOW_WHILE_IDLE")
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d("ReminderManager", "Scheduled exact alarm (older Android)")
            }
        } catch (e: Exception) {
            Log.e("ReminderManager", "Error scheduling alarm: ${e.message}")

            try {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
                Log.d("ReminderManager", "Fallback: Scheduled repeating alarm")
            } catch (e: Exception) {
                Log.e("ReminderManager", "Error in fallback scheduling: ${e.message}")
            }
        }
    }

    fun cancelDailyReminder() {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_READING_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("ReminderManager", "Daily reminder canceled")
        }
    }


    class ReminderReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("ReminderReceiver", "Received reading reminder broadcast: ${intent.action}")

            if (intent.action == ACTION_READING_REMINDER) {
                Log.d("ReminderReceiver", "Processing reading reminder")

                val notificationHelper = NotificationHelper(context)
                notificationHelper.showReadingReminderNotification()

                val reminderManager = ReminderManager(context)
                reminderManager.scheduleDailyReminder()
            }
        }
    }
}