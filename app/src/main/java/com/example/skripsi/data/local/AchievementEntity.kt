package com.example.skripsi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.skripsi.data.repository.Repository

// Achievement entity for storing user achievements
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,  // Format: achievementId_userId
    val userId: String,
    val achievementId: String,
    val title: String,
    val description: String,
    val iconResource: Int,
    val unlockedDate: Long,
    val displayed: Boolean = false  // To track if achievement has been shown to user
)

// Achievement definition class (not stored in database, used to define possible achievements)
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconResource: Int,
    val checkUnlockCondition: suspend (Repository, String) -> Boolean  // Suspend function to check if achievement should be unlocked
)