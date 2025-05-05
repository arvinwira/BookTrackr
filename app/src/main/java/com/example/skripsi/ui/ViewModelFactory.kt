package com.example.skripsi.ui

import com.example.skripsi.ui.tracking.wanttoread.WantToReadViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.skripsi.data.repository.Repository
import com.example.skripsi.ui.bookdetail.BookDetailViewModel
import com.example.skripsi.ui.goals.GoalsViewModel
import com.example.skripsi.ui.home.HomeViewModel
import com.example.skripsi.ui.reading.ReadingViewModel
import com.example.skripsi.ui.search.SearchViewModel
import com.example.skripsi.ui.tracking.currentlyreading.CurrentlyReadingViewModel
import com.example.skripsi.ui.tracking.finishedreading.FinishedReadingViewModel

class ViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(repository) as T
            }
            modelClass.isAssignableFrom(BookDetailViewModel::class.java) -> {
                BookDetailViewModel(repository) as T
            }
            modelClass.isAssignableFrom(CurrentlyReadingViewModel::class.java) -> {
                CurrentlyReadingViewModel(repository) as T
            }
            modelClass.isAssignableFrom(WantToReadViewModel::class.java) -> {
                WantToReadViewModel(repository) as T
            }
            modelClass.isAssignableFrom(FinishedReadingViewModel::class.java) -> {
                FinishedReadingViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ReadingViewModel::class.java) -> {
                ReadingViewModel(repository) as T
            }
            modelClass.isAssignableFrom(GoalsViewModel::class.java) -> {
                GoalsViewModel(repository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}