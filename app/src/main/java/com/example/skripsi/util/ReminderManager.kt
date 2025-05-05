package com.example.skripsi.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

/**
 * Manager class to schedule and handle daily reading reminders
 */
class ReminderManager(private val context: Context) {

    companion object {
        const val ACTION_READING_REMINDER = "com.example.skripsi.ACTION_READING_REMINDER"
        private const val REMINDER_REQUEST_CODE = 2001
    }

    /**
     * Schedule a daily reminder at 5:00 PM if enabled in preferences
     * If testing is true, the notification will trigger after 30 seconds instead
     */
    fun scheduleDailyReminder(testing: Boolean = false) {
        val preferenceManager = PreferenceManager(context)

        // Only schedule if reminders are enabled
        if (!preferenceManager.isDailyReminderEnabled()) {
            Log.d("ReminderManager", "Daily reading reminders are disabled in preferences")
            return
        }

        // Create intent for the reminder broadcast receiver
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_READING_REMINDER
        }

        // Create pending intent that will be triggered when alarm fires
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Get the AlarmManager service
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()

        if (testing) {
            // For testing: Set alarm to 30 seconds from now
            calendar.timeInMillis = System.currentTimeMillis() + 30 * 1000
            Log.d("ReminderManager", "TEST MODE: Scheduling reminder for 30 seconds from now")
        } else {
            // Regular mode: Set for 5:00 PM today or tomorrow
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, 17) // 5 PM (24-hour format)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            // If it's already past 5 PM today, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Log the scheduled time
        Log.d("ReminderManager", "Scheduling reminder for: ${calendar.time}")

        // Choose the appropriate scheduling method based on API level
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !testing) {
                // For Android 12+, check if we can schedule exact alarms
                if (alarmManager.canScheduleExactAlarms()) {
                    // We have permission for exact alarms
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("ReminderManager", "Scheduled exact alarm with ALLOW_WHILE_IDLE")
                } else {
                    // We don't have permission, use inexact alarm
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                    Log.d("ReminderManager", "Scheduled inexact repeating alarm (no permission for exact)")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For Android 6.0 to 11, use setExactAndAllowWhileIdle
                if (testing) {
                    // For test alarms, just use a one-time exact alarm
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("ReminderManager", "Scheduled one-time test alarm")
                } else {
                    // For regular alarms, set daily repeat
                    // Note: We'll need to reschedule this each time it fires for true daily repeats
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("ReminderManager", "Scheduled exact alarm with ALLOW_WHILE_IDLE")
                }
            } else {
                // For older Android versions (pre-6.0)
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d("ReminderManager", "Scheduled exact alarm (older Android)")
            }
        } catch (e: Exception) {
            Log.e("ReminderManager", "Error scheduling alarm: ${e.message}")

            // Fallback to setRepeating if all else fails
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

    /**
     * Schedule a test notification for immediate testing (30 seconds from now)
     */
    fun scheduleTestReminder() {
        scheduleDailyReminder(testing = true)
    }

    /**
     * Cancel the daily reminder
     */
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

    /**
     * Broadcast receiver for the reading reminder
     */
    class ReminderReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("ReminderReceiver", "Received reading reminder broadcast: ${intent.action}")

            if (intent.action == ACTION_READING_REMINDER) {
                Log.d("ReminderReceiver", "Processing reading reminder")

                // Show the notification
                val notificationHelper = NotificationHelper(context)
                notificationHelper.showReadingReminderNotification()

                // Reschedule for tomorrow (for APIs that don't support setRepeating properly)
                val reminderManager = ReminderManager(context)
                reminderManager.scheduleDailyReminder()
            }
        }
    }
}