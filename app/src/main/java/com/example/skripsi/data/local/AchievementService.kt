package com.example.skripsi.data.local

import android.util.Log
import com.example.skripsi.R
import com.example.skripsi.data.repository.Repository

class AchievementService(private val repository: Repository) {

    val achievements = listOf(
        Achievement(
            id = "first_progress",
            title = "First Steps",
            description = "Start reading your first book",
            iconResource = R.drawable.trophy,
            checkUnlockCondition = { repo, userId ->
                repo.getCurrentlyReadingBooks(userId).isNotEmpty()
            }
        ),
        Achievement(
            id = "finish_first_book",
            title = "First Finish",
            description = "Complete your first book",
            iconResource = R.drawable.trophy,
            checkUnlockCondition = { repo, userId ->
                repo.getFinishedReadingBooks(userId).isNotEmpty()
            }
        ),
        Achievement(
            id = "finish_5_books",
            title = "Bookworm",
            description = "Complete 5 books",
            iconResource = R.drawable.trophy,
            checkUnlockCondition = { repo, userId ->
                repo.getFinishedReadingBooks(userId).size >= 5
            }
        ),
        Achievement(
            id = "finish_10_books",
            title = "Book Collector",
            description = "Complete 10 books",
            iconResource = R.drawable.trophy,
            checkUnlockCondition = { repo, userId ->
                repo.getFinishedReadingBooks(userId).size >= 10
            }
        ),
        Achievement(
            id = "finish_25_books",
            title = "Library Master",
            description = "Complete 25 books",
            iconResource = R.drawable.trophy,
            checkUnlockCondition = { repo, userId ->
                repo.getFinishedReadingBooks(userId).size >= 25
            }
        ),

        Achievement(
            id = "bookworm_pro",
            title = "Bookworm Pro",
            description = "Complete 50 books",
            iconResource = R.drawable.trophy,
            checkUnlockCondition = { repo, userId ->
                repo.getFinishedReadingBooks(userId).size >= 50
            }
        ),

        Achievement(
            id = "complete_goal",
            title = "Goal Crusher",
            description = "Complete your annual reading goal",
            iconResource = R.drawable.trophy,
            checkUnlockCondition = { repo, userId ->
                val goal = repo.getUserAnnualGoal(userId)
                val booksRead = repo.getFinishedReadingBooks(userId).size
                booksRead >= goal && goal > 0
            }
        ),

        Achievement(
            id = "reading_variety",
            title = "Busy Reader",
            description = "Have 3 books in progress simultaneously",
            iconResource = R.drawable.trophy,
            checkUnlockCondition = { repo, userId ->
                repo.getCurrentlyReadingBooks(userId).size >= 3
            }
        ),

        Achievement(
            id = "wishlist_collector",
            title = "Wishlist Collector",
            description = "Add 10 books to your Want to Read list",
            iconResource = R.drawable.trophy,
            checkUnlockCondition = { repo, userId ->
                repo.getWantToReadBooks(userId).size >= 10
            }
        ),
        Achievement(
            id = "reading_variety",
            title = "Reading Variety",
            description = "Have books in all three lists: Currently Reading, Want to Read, and Finished Reading",
            iconResource = R.drawable.trophy,
            checkUnlockCondition = { repo, userId ->
                repo.getCurrentlyReadingBooks(userId).isNotEmpty() &&
                        repo.getWantToReadBooks(userId).isNotEmpty() &&
                        repo.getFinishedReadingBooks(userId).isNotEmpty()
            }
        )

    )

    suspend fun checkAndUnlockAchievements(userId: String): List<AchievementEntity> {
        val newlyUnlocked = mutableListOf<AchievementEntity>()

        for (achievement in achievements) {
            try {
                if (repository.getAchievement(userId, achievement.id) != null) {
                    continue
                }

                if (achievement.checkUnlockCondition(repository, userId)) {
                    val achievementEntity = AchievementEntity(
                        id = "${achievement.id}_$userId",
                        userId = userId,
                        achievementId = achievement.id,
                        title = achievement.title,
                        description = achievement.description,
                        iconResource = achievement.iconResource,
                        unlockedDate = System.currentTimeMillis(),
                        displayed = false
                    )
                    repository.saveAchievement(achievementEntity)
                    newlyUnlocked.add(achievementEntity)

                    Log.d("AchievementService", "Unlocked new achievement: ${achievement.title}")
                }
            } catch (e: Exception) {
                Log.e("AchievementService", "Error checking achievement ${achievement.id}: ${e.message}")
            }
        }

        return newlyUnlocked
    }
    suspend fun getUserAchievementsWithStatus(userId: String): List<Pair<Achievement, Boolean>> {
        val unlockedAchievements = repository.getUserAchievements(userId)
        val unlockedIds = unlockedAchievements.map { it.achievementId }.toSet()

        return achievements.map { achievement ->
            Pair(achievement, achievement.id in unlockedIds)
        }
    }
    suspend fun markAchievementsAsDisplayed(achievements: List<AchievementEntity>) {
        for (achievement in achievements) {
            repository.markAchievementAsDisplayed(achievement.id)
        }
    }
}