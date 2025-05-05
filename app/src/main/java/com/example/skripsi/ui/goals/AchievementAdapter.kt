package com.example.skripsi.ui.goals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsi.R
import com.example.skripsi.data.local.Achievement

class AchievementAdapter(
    private val achievements: List<Pair<Achievement, Boolean>>
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    class AchievementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.achievementIcon)
        val title: TextView = view.findViewById(R.id.achievementTitle)
        val description: TextView = view.findViewById(R.id.achievementDescription)
        val lockedOverlay: View = view.findViewById(R.id.lockedOverlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val (achievement, isUnlocked) = achievements[position]

        holder.icon.setImageResource(achievement.iconResource)

        holder.title.text = achievement.title
        holder.description.text = achievement.description

        if (isUnlocked) {
            holder.lockedOverlay.visibility = View.GONE
            holder.icon.setBackgroundResource(R.drawable.achievement_circle_background)
        } else {
            holder.lockedOverlay.visibility = View.VISIBLE
            holder.icon.setBackgroundResource(R.drawable.achievement_locked_background)
            holder.title.alpha = 0.5f
            holder.description.alpha = 0.5f
        }
    }

    override fun getItemCount() = achievements.size
}