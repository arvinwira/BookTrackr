package com.example.skripsi.util

import android.Manifest
import android.app.AlarmManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Helper class to manage notification permissions
 */
class NotificationPermissionHelper(private val context: Context) {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 123

        // Determine if notification permission is required based on Android version
        fun isPermissionRequired(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        }

        // Check if notification permission has been granted
        fun hasNotificationPermission(context: Context): Boolean {
            // For Android 13 and higher, check for the POST_NOTIFICATIONS permission
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                // For earlier Android versions, permission is granted at app install
                true
            }
        }
    }

    /**
     * Request notification permission if needed
     * @param activity The activity to request permission from
     * @return True if permission is already granted, false if it needs to be requested
     */
    fun requestNotificationPermissionIfNeeded(activity: Activity): Boolean {
        if (!isPermissionRequired()) {
            // Permission not required for this Android version
            return true
        }

        if (hasNotificationPermission(context)) {
            // Permission already granted
            return true
        }

        // Permission needs to be requested
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST_CODE
        )
        return false
    }

    /**
     * Request notification permission if needed
     * @param fragment The fragment to request permission from
     * @return True if permission is already granted, false if it needs to be requested
     */
    fun requestNotificationPermissionIfNeeded(fragment: Fragment): Boolean {
        if (!isPermissionRequired()) {
            // Permission not required for this Android version
            return true
        }

        if (hasNotificationPermission(context)) {
            // Permission already granted
            return true
        }

        // Permission needs to be requested
        fragment.requestPermissions(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST_CODE
        )
        return false
    }

    /**
     * Process permission result
     * @return True if permission was granted, false otherwise
     */
    fun processPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("NotificationPermission", "Notification permission granted")
                return true
            } else {
                // Permission denied
                Log.d("NotificationPermission", "Notification permission denied")
                return false
            }
        }
        return false
    }

    /**
     * Open the app's notification settings in system settings
     */
    fun openNotificationSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    /**
     * Show a dialog explaining why notification permission is needed
     * This should be shown before requesting permission for better user experience
     */
    fun showNotificationPermissionRationale(activity: Activity, onProceed: () -> Unit) {
        // You can implement a custom dialog here, but for simplicity,
        // let's just use a Toast and proceed with the request
        Toast.makeText(
            activity,
            "Notification permission is needed to send you daily reading reminders",
            Toast.LENGTH_LONG
        ).show()

        // Proceed with permission request after showing rationale
        onProceed()
    }

    /**
     * Check if we have permission to schedule exact alarms (Android 12+)
     */
    fun hasExactAlarmPermission(context: Context): Boolean {
        // For Android 12+, check for exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        // Permission not required for earlier Android versions
        return true
    }

    /**
     * Open exact alarm permission settings
     */
    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent().apply {
                action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            }
            context.startActivity(intent)
        }
    }
}