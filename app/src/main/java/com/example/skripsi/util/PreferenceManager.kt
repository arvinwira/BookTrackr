package com.example.skripsi.util

import android.content.Context
import android.content.SharedPreferences


class PreferenceManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "reading_app_preferences"
        private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
        private const val KEY_APP_LANGUAGE = "app_language" // New key for language preference
        private const val DEFAULT_DAILY_REMINDER_ENABLED = true
        private const val DEFAULT_APP_LANGUAGE = "en" // Default language is English
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    fun isDailyReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, DEFAULT_DAILY_REMINDER_ENABLED)
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled).apply()
    }

}