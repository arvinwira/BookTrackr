package com.example.skripsi.ui.reading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skripsi.data.repository.Repository
import kotlinx.coroutines.launch

class ReadingViewModel(private val repository: Repository) : ViewModel() {

    private val _readingProgress = MutableLiveData<Int>()
    val readingProgress: LiveData<Int> = _readingProgress

    fun getReadingProgress(bookId: String, userId: String) {
        viewModelScope.launch {
            val progress = repository.getReadingProgress(bookId, userId)
            _readingProgress.postValue(progress)
        }
    }

    fun updateReadingProgress(bookId: String, userId: String, progress: Int) {
        viewModelScope.launch {
            repository.updateReadingProgress(bookId, userId, progress)
        }
    }


}