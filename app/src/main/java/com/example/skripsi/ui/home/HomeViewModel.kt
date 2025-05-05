package com.example.skripsi.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.skripsi.data.remote.Book
import com.example.skripsi.data.repository.Repository

class HomeViewModel(private val repository: Repository) : ViewModel() {

    val recentBooks: LiveData<List<Book>> = repository.recentBooks
    val error: LiveData<String> = repository.error

    init {
        repository.fetchRecentBooks()
    }
}
