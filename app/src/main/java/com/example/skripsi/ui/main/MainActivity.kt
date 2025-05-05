package com.example.skripsi.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.skripsi.R
import com.example.skripsi.databinding.ActivityMainBinding
import com.example.skripsi.ui.ThemeHelper
import com.example.skripsi.util.NotificationPermissionHelper
import com.example.skripsi.util.PreferenceManager
import com.example.skripsi.util.ReminderManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var doubleBackToExitPressedOnce = false

    // Add these properties for notification handling
    private lateinit var notificationPermissionHelper: NotificationPermissionHelper
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var reminderManager: ReminderManager

    override fun onCreate(savedInstanceState: Bundle?) {

        ThemeHelper.forceLightMode()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize notification helpers
        notificationPermissionHelper = NotificationPermissionHelper(this)
        preferenceManager = PreferenceManager(this)
        reminderManager = ReminderManager(this)

        // Find the NavHostFragment and get the NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: throw IllegalStateException("NavHostFragment not found")
        navController = navHostFragment.navController

        // Set up BottomNavigationView with NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // Check if notifications are enabled in preferences
        if (preferenceManager.isDailyReminderEnabled()) {
            // Request notification permission (if needed) and setup reminders
            setupNotifications()
        }
    }

    private fun setupNotifications() {
        // Show rationale before requesting permission (for better UX)
        if (!NotificationPermissionHelper.hasNotificationPermission(this) &&
            NotificationPermissionHelper.isPermissionRequired()) {

            notificationPermissionHelper.showNotificationPermissionRationale(this) {
                // This will be called after showing the rationale
                if (notificationPermissionHelper.requestNotificationPermissionIfNeeded(this)) {
                    // Permission already granted, schedule reminder
                    reminderManager.scheduleDailyReminder()
                }
                // If permission not granted, we'll handle it in onRequestPermissionsResult
            }
        } else {
            // Permission already granted or not required, schedule reminder
            reminderManager.scheduleDailyReminder()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Process notification permission result
        val granted = notificationPermissionHelper.processPermissionResult(
            requestCode, permissions, grantResults
        )

        if (granted) {
            // Permission granted, schedule reminders
            reminderManager.scheduleDailyReminder()
            Toast.makeText(this, "Reading reminders are now enabled", Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied, update preferences
            preferenceManager.setDailyReminderEnabled(false)
            Toast.makeText(this,
                "Notification permission denied. Daily reminders won't work.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}