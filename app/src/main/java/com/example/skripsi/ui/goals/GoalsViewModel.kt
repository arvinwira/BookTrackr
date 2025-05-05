package com.example.skripsi.ui.goals

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skripsi.data.local.Achievement
import com.example.skripsi.data.local.AchievementEntity
import com.example.skripsi.data.repository.Repository
import com.example.skripsi.data.local.AchievementService
import kotlinx.coroutines.launch

class GoalsViewModel(private val repository: Repository) : ViewModel() {

    private val _annualGoal = MutableLiveData<Int>()
    val annualGoal: LiveData<Int> = _annualGoal
    private val _finishedBooksCount = MutableLiveData<Int>()
    val finishedBooksCount: LiveData<Int> = _finishedBooksCount
    private val _currentlyReadingCount = MutableLiveData<Int>()
    val currentlyReadingCount: LiveData<Int> = _currentlyReadingCount
    private val _newAchievements = MutableLiveData<List<AchievementEntity>>()
    val newAchievements: LiveData<List<AchievementEntity>> = _newAchievements
    private val _achievements = MutableLiveData<List<Pair<Achievement, Boolean>>>()
    val achievements: LiveData<List<Pair<Achievement, Boolean>>> = _achievements
    private val _recentAchievements = MutableLiveData<List<AchievementEntity>>()
    val recentAchievements: LiveData<List<AchievementEntity>> = _recentAchievements
    private val achievementService = AchievementService(repository)

    fun loadReadingStats(userId: String) {
        viewModelScope.launch {
            try {
                val goal = repository.getUserAnnualGoal(userId)
                _annualGoal.postValue(goal)

                val finishedBooks = repository.getFinishedReadingBooks(userId)
                _finishedBooksCount.postValue(finishedBooks.size)

                val currentlyReading = repository.getCurrentlyReadingBooks(userId)
                _currentlyReadingCount.postValue(currentlyReading.size)

                checkForNewAchievements(userId)

                loadRecentAchievements(userId)
            } catch (e: Exception) {
                Log.e("GoalsViewModel", "Error loading reading stats: ${e.message}", e)
            }
        }
    }

    fun updateAnnualGoal(userId: String, goal: Int) {
        viewModelScope.launch {
            repository.saveUserAnnualGoal(userId, goal)
            _annualGoal.postValue(goal)

            checkForNewAchievements(userId)
        }
    }

    private suspend fun checkForNewAchievements(userId: String) {
        try {
            val newlyUnlocked = achievementService.checkAndUnlockAchievements(userId)
            if (newlyUnlocked.isNotEmpty()) {
                val undisplayedAchievements = repository.getNewAchievements(userId)
                if (undisplayedAchievements.isNotEmpty()) {
                    _newAchievements.postValue(undisplayedAchievements)
                }
            }
        } catch (e: Exception) {
            Log.e("GoalsViewModel", "Error checking achievements: ${e.message}", e)
        }
    }

    fun loadAchievements(userId: String) {
        viewModelScope.launch {
            try {
                val achievementsWithStatus = achievementService.getUserAchievementsWithStatus(userId)
                _achievements.postValue(achievementsWithStatus)

                checkForNewAchievements(userId)
            } catch (e: Exception) {
                Log.e("GoalsViewModel", "Error loading achievements: ${e.message}", e)
            }
        }
    }

    fun loadRecentAchievements(userId: String) {
        viewModelScope.launch {
            try {
                val allAchievements = repository.getUserAchievements(userId)
                val recent = allAchievements.sortedByDescending { it.unlockedDate }.take(3)
                _recentAchievements.postValue(recent)
            } catch (e: Exception) {
                Log.e("GoalsViewModel", "Error loading recent achievements: ${e.message}", e)
            }
        }
    }

    fun markAchievementsAsDisplayed(achievements: List<AchievementEntity>) {
        viewModelScope.launch {
            achievementService.markAchievementsAsDisplayed(achievements)
        }
    }
}