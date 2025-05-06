package com.example.skripsi.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device boot completed, checking reading reminder settings")

            val preferenceManager = PreferenceManager(context)
            if (preferenceManager.isDailyReminderEnabled()) {
                Log.d("BootReceiver", "Reminders are enabled, rescheduling")

                val reminderManager = ReminderManager(context)
                reminderManager.scheduleDailyReminder()
            } else {
                Log.d("BootReceiver", "Reminders are disabled, not rescheduling")
            }
        }
    }
}