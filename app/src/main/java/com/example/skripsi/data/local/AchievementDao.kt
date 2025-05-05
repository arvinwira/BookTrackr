package com.example.skripsi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements WHERE userId = :userId")
    suspend fun getUserAchievements(userId: String): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE userId = :userId AND achievementId = :achievementId")
    suspend fun getAchievement(userId: String, achievementId: String): AchievementEntity?

    @Query("SELECT * FROM achievements WHERE userId = :userId AND displayed = 0")
    suspend fun getNewAchievements(userId: String): List<AchievementEntity>

    @Query("UPDATE achievements SET displayed = 1 WHERE id = :id")
    suspend fun markAchievementAsDisplayed(id: String)
}