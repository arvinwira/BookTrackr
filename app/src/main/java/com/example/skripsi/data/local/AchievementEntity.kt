package com.example.skripsi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.skripsi.data.repository.Repository

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val achievementId: String,
    val title: String,
    val description: String,
    val iconResource: Int,
    val unlockedDate: Long,
    val displayed: Boolean = false
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconResource: Int,
    val checkUnlockCondition: suspend (Repository, String) -> Boolean
)