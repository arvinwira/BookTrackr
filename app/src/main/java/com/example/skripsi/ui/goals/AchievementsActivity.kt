package com.example.skripsi.ui.goals

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsi.R
import com.example.skripsi.data.repository.Repository
import com.example.skripsi.databinding.ActivityAchievementsBinding
import com.example.skripsi.ui.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class AchievementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsBinding
    private val viewModel: GoalsViewModel by viewModels {
        ViewModelFactory(Repository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "My Achievements"
        }

        binding.achievementsRecyclerView.layoutManager = LinearLayoutManager(this)


        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { viewModel.loadAchievements(it) }

        viewModel.achievements.observe(this) { achievements ->
            val adapter = AchievementAdapter(achievements)
            binding.achievementsRecyclerView.adapter = adapter

            val unlockedCount = achievements.count { it.second }
            val totalCount = achievements.size
            val progressPercent = (unlockedCount.toFloat() / totalCount * 100).toInt()

            binding.achievementProgressBar.progress = progressPercent
            binding.achievementProgressText.text = "$unlockedCount / $totalCount"

            val trophyRes = when {
                progressPercent >= 100 -> R.drawable.trophy
                progressPercent >= 50 -> R.drawable.trophy
                progressPercent >= 25 -> R.drawable.trophy
                else -> R.drawable.trophy
            }
            binding.trophyIcon.setImageResource(trophyRes)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}