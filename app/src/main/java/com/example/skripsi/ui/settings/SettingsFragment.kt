package com.example.skripsi.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.skripsi.databinding.FragmentSettingsBinding
import com.example.skripsi.ui.auth.WelcomeActivity
import com.example.skripsi.util.NotificationHelper
import com.example.skripsi.util.NotificationPermissionHelper
import com.example.skripsi.util.PreferenceManager
import com.example.skripsi.util.ReminderManager

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by viewModels()

    // Add these properties
    private lateinit var notificationPermissionHelper: NotificationPermissionHelper
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var reminderManager: ReminderManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize helpers
        notificationPermissionHelper = NotificationPermissionHelper(requireContext())
        preferenceManager = PreferenceManager(requireContext())
        reminderManager = ReminderManager(requireContext())

        settingsViewModel.email.observe(viewLifecycleOwner) { email ->
            binding.tvEmail.text = "Email: $email"
        }

        settingsViewModel.fetchEmail()

        // Set up notification switch
        binding.notificationSwitch.isChecked = preferenceManager.isDailyReminderEnabled()
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // User wants to enable notifications
                enableNotifications()
            } else {
                // User wants to disable notifications
                disableNotifications()
            }
        }

        binding.appGuideButton.setOnClickListener {
            val intent = Intent(activity, AppGuideActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            settingsViewModel.logout()
            val intent = Intent(activity, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }

    private fun enableNotifications() {
        // Check if we have permission
        if (notificationPermissionHelper.requestNotificationPermissionIfNeeded(this)) {
            // Permission granted or not needed, enable notifications
            preferenceManager.setDailyReminderEnabled(true)
            reminderManager.scheduleDailyReminder()
            Toast.makeText(requireContext(),
                "Daily reading reminders enabled",
                Toast.LENGTH_SHORT).show()
        }
        // If permission not granted, we'll handle in onRequestPermissionsResult
    }

    private fun disableNotifications() {
        preferenceManager.setDailyReminderEnabled(false)
        reminderManager.cancelDailyReminder()
        Toast.makeText(requireContext(),
            "Daily reading reminders disabled",
            Toast.LENGTH_SHORT).show()
    }

    private fun askToOpenSettings() {
        // In a real app, you might want to use a proper dialog here
        Toast.makeText(requireContext(),
            "Would you like to open settings to enable notifications?",
            Toast.LENGTH_LONG).show()

        // For demo purposes, we'll just open settings directly
        notificationPermissionHelper.openNotificationSettings()
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
            // Permission granted, update UI and enable notifications
            binding.notificationSwitch.isChecked = true
            preferenceManager.setDailyReminderEnabled(true)
            reminderManager.scheduleDailyReminder()
            Toast.makeText(requireContext(),
                "Notification permission granted",
                Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied, update UI
            binding.notificationSwitch.isChecked = false
            preferenceManager.setDailyReminderEnabled(false)
            Toast.makeText(requireContext(),
                "Notification permission denied. Daily reminders won't work.",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}