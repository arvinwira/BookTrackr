package com.example.skripsi.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user preferences for the application
 */
class PreferenceManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "reading_app_preferences"
        private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
        private const val KEY_APP_LANGUAGE = "app_language" // New key for language preference

        // Default values
        private const val DEFAULT_DAILY_REMINDER_ENABLED = true
        private const val DEFAULT_APP_LANGUAGE = "en" // Default language is English
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if daily reading reminders are enabled
     */
    fun isDailyReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, DEFAULT_DAILY_REMINDER_ENABLED)
    }

    /**
     * Enable or disable daily reading reminders
     */
    fun setDailyReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled).apply()
    }

    /**
     * Get the current app language code (en for English, id for Indonesian)
     */
    fun getAppLanguage(): String {
        return prefs.getString(KEY_APP_LANGUAGE, DEFAULT_APP_LANGUAGE) ?: DEFAULT_APP_LANGUAGE
    }

    /**
     * Set the app language
     * @param languageCode "en" for English, "id" for Indonesian
     */
    fun setAppLanguage(languageCode: String) {
        prefs.edit().putString(KEY_APP_LANGUAGE, languageCode).apply()
    }
}