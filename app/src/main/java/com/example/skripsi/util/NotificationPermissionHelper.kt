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


class NotificationPermissionHelper(private val context: Context) {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 123

        fun isPermissionRequired(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        }

        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
    }

    fun requestNotificationPermissionIfNeeded(activity: Activity): Boolean {
        if (!isPermissionRequired()) {
            return true
        }

        if (hasNotificationPermission(context)) {
            return true
        }

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST_CODE
        )
        return false
    }


    fun requestNotificationPermissionIfNeeded(fragment: Fragment): Boolean {
        if (!isPermissionRequired()) {
            return true
        }

        if (hasNotificationPermission(context)) {
            return true
        }

        fragment.requestPermissions(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST_CODE
        )
        return false
    }


    fun processPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("NotificationPermission", "Notification permission granted")
                return true
            } else {
                Log.d("NotificationPermission", "Notification permission denied")
                return false
            }
        }
        return false
    }


    fun showNotificationPermissionRationale(activity: Activity, onProceed: () -> Unit) {
        Toast.makeText(
            activity,
            "Notification permission is needed to send you daily reading reminders",
            Toast.LENGTH_LONG
        ).show()

        onProceed()
    }


}