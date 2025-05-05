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

    private lateinit var notificationPermissionHelper: NotificationPermissionHelper
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var reminderManager: ReminderManager

    override fun onCreate(savedInstanceState: Bundle?) {

        ThemeHelper.forceLightMode()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationPermissionHelper = NotificationPermissionHelper(this)
        preferenceManager = PreferenceManager(this)
        reminderManager = ReminderManager(this)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: throw IllegalStateException("NavHostFragment not found")
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        if (preferenceManager.isDailyReminderEnabled()) {
            setupNotifications()
        }
    }

    private fun setupNotifications() {
        if (!NotificationPermissionHelper.hasNotificationPermission(this) &&
            NotificationPermissionHelper.isPermissionRequired()) {

            notificationPermissionHelper.showNotificationPermissionRationale(this) {
                if (notificationPermissionHelper.requestNotificationPermissionIfNeeded(this)) {
                    reminderManager.scheduleDailyReminder()
                }
            }
        } else {
            reminderManager.scheduleDailyReminder()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val granted = notificationPermissionHelper.processPermissionResult(
            requestCode, permissions, grantResults
        )

        if (granted) {
            reminderManager.scheduleDailyReminder()
            Toast.makeText(this, "Reading reminders are now enabled", Toast.LENGTH_SHORT).show()
        } else {
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