package com.example.skripsi.ui

import androidx.appcompat.app.AppCompatDelegate


object ThemeHelper {

    fun forceLightMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}