package com.example.skripsi.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receiver that gets triggered when the device boots
 * Used to reschedule our reading reminders
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device boot completed, checking reading reminder settings")

            // Check if reminders are enabled
            val preferenceManager = PreferenceManager(context)
            if (preferenceManager.isDailyReminderEnabled()) {
                Log.d("BootReceiver", "Reminders are enabled, rescheduling")

                // Reschedule the daily reminder
                val reminderManager = ReminderManager(context)
                reminderManager.scheduleDailyReminder()
            } else {
                Log.d("BootReceiver", "Reminders are disabled, not rescheduling")
            }
        }
    }
}