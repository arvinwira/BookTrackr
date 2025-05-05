package com.example.skripsi.ui.goals

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.skripsi.R
import com.example.skripsi.data.local.AchievementEntity
import com.example.skripsi.data.repository.Repository
import com.example.skripsi.databinding.FragmentGoalsBinding
import com.example.skripsi.ui.ViewModelFactory
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GoalsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = Repository(requireContext())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[GoalsViewModel::class.java]

        setupObservers()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { viewModel.loadReadingStats(it) }

        binding.editGoalButton.setOnClickListener {
            showGoalEditDialog()
        }

        binding.viewAllAchievementsButton.setOnClickListener {
            val intent = Intent(requireContext(), AchievementsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        viewModel.annualGoal.observe(viewLifecycleOwner) { goal ->
            binding.annualGoalText.text = "$goal books"
        }

        viewModel.finishedBooksCount.observe(viewLifecycleOwner) { count ->
            binding.booksReadText.text = "$count books"

            viewModel.annualGoal.value?.let { goal ->
                val progress =
                    if (goal > 0) (count.toFloat() / goal.toFloat() * 100).roundToInt() else 0
                binding.annualGoalProgress.progress = progress
                binding.goalProgressText.text = "$progress%"
            }
        }

        viewModel.currentlyReadingCount.observe(viewLifecycleOwner) { count ->
            binding.booksInProgressText.text = "$count books"
        }

        viewModel.newAchievements.observe(viewLifecycleOwner) { achievements ->
            if (achievements.isNotEmpty()) {
                showAchievementPopup(achievements.first())
                viewModel.markAchievementsAsDisplayed(achievements)
            }
        }

        viewModel.recentAchievements.observe(viewLifecycleOwner) { achievements ->
            updateRecentAchievementsDisplay(achievements)
        }
    }

    override fun onResume() {
        super.onResume()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            Log.d("GoalsFragment", "Reloading reading stats on resume for user: $userId")
            viewModel.loadReadingStats(it)
        }
    }

    private fun updateRecentAchievementsDisplay(achievements: List<AchievementEntity>) {
        binding.recentAchievementsLayout.removeAllViews()

        if (achievements.isEmpty()) {
            val placeholder = TextView(requireContext())
            placeholder.text = "Complete goals to earn achievements!"
            placeholder.textAlignment = View.TEXT_ALIGNMENT_CENTER
            binding.recentAchievementsLayout.addView(placeholder)
            return
        }

        for (achievement in achievements) {
            val imageView = ImageView(requireContext())
            val params = ViewGroup.MarginLayoutParams(
                resources.getDimensionPixelSize(R.dimen.achievement_icon_size),
                resources.getDimensionPixelSize(R.dimen.achievement_icon_size)
            )
            params.marginEnd = resources.getDimensionPixelSize(R.dimen.achievement_icon_margin)
            imageView.layoutParams = params
            imageView.setImageResource(achievement.iconResource)
            imageView.background =
                resources.getDrawable(R.drawable.achievement_circle_background, null)
            imageView.contentDescription = achievement.title
            imageView.setOnClickListener {
                showAchievementPopup(achievement)
            }
            binding.recentAchievementsLayout.addView(imageView)
        }
    }

    private fun showGoalEditDialog() {
        val currentGoal = viewModel.annualGoal.value ?: 20

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_goal, null)
        val goalSlider = dialogView.findViewById<Slider>(R.id.goalSlider)
        val selectedGoalText = dialogView.findViewById<TextView>(R.id.selectedGoalText)

        goalSlider.value = currentGoal.toFloat()
        selectedGoalText.text = "Selected: $currentGoal books"

        goalSlider.addOnChangeListener { _, value, _ ->
            selectedGoalText.text = "Selected: ${value.toInt()} books"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Set Annual Reading Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newGoal = goalSlider.value.toInt()
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                userId?.let {
                    viewModel.updateAnnualGoal(it, newGoal)
                    Toast.makeText(
                        requireContext(),
                        "Goal updated to $newGoal books",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAchievementPopup(achievement: AchievementEntity) {
        val dialogView = layoutInflater.inflate(R.layout.achievement_popup, null)

        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)

        dialogView.findViewById<TextView>(R.id.popupAchievementTitle).text = achievement.title
        dialogView.findViewById<TextView>(R.id.popupAchievementDescription).text =
            achievement.description
        dialogView.findViewById<ImageView>(R.id.popupAchievementIcon)
            .setImageResource(achievement.iconResource)

        dialogView.findViewById<Button>(R.id.popupCloseButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}