package com.example.skripsi.ui.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.skripsi.databinding.ActivityAppGuideBinding

class AppGuideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}