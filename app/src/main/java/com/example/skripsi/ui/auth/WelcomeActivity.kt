package com.example.skripsi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.skripsi.R
import com.example.skripsi.databinding.ActivityWelcomeBinding
import com.example.skripsi.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var walkthroughAdapter: WalkthroughAdapter
    private lateinit var indicators: Array<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (firebaseAuth.currentUser != null) {
            navigateToMainActivity()
        }

        setupWalkthrough()

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }


    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setupWalkthrough() {
        val walkthroughItems = listOf(
            WalkthroughAdapter.WalkthroughItem(
                R.drawable.walkthrough_search,
                getString(R.string.walkthroughtitle1),
                getString(R.string.walkthroughdesc1)
            ),
            WalkthroughAdapter.WalkthroughItem(
                R.drawable.walkthrough_read,
                getString(R.string.walkthroughtitle2),
                getString(R.string.walkthroughdesc2)
            ),
            WalkthroughAdapter.WalkthroughItem(
                R.drawable.walkthrough_goal,
                getString(R.string.walkthroughtitle3),
                getString(R.string.walkthroughdesc3)
            )
        )

        walkthroughAdapter = WalkthroughAdapter(walkthroughItems)
        binding.walkthroughViewPager.adapter = walkthroughAdapter

        setupIndicators(walkthroughItems.size)
        setCurrentIndicator(0)

        binding.walkthroughViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })
    }

    private fun setupIndicators(count: Int) {
        try {
            indicators = Array(count) { _ ->
                val indicator = ImageView(applicationContext)
                val layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(8, 0, 8, 0)
                indicator.layoutParams = layoutParams
                indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                binding.indicatorLayout.addView(indicator)
                indicator
            }
        } catch (e: Exception) {
            Log.e("WelcomeActivity", "Error setting up indicators: ${e.message}")
        }
    }

    private fun setCurrentIndicator(position: Int) {
        try {
            for (i in indicators.indices) {
                indicators[i].setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        if (i == position) R.drawable.indicator_active else R.drawable.indicator_inactive
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("WelcomeActivity", "Error setting indicator: ${e.message}")
        }
    }
}