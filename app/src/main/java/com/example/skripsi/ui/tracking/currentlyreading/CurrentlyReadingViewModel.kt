package com.example.skripsi.ui.tracking.currentlyreading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skripsi.data.local.BookEntity
import com.example.skripsi.data.repository.Repository
import com.example.skripsi.ui.tracking.currentlyreading.CurrentlyReadingAdapter.BookWithProgress
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

class CurrentlyReadingViewModel(private val repository: Repository) : ViewModel() {

    private val _books = MutableLiveData<List<BookWithProgress>>()
    val books: LiveData<List<BookWithProgress>> get() = _books

    fun loadCurrentlyReadingBooks(userId: String) {
        viewModelScope.launch {
            val bookEntities = repository.getCurrentlyReadingBooks(userId)

            val booksWithProgress = bookEntities.map { book ->
                val progress = async { repository.getReadingProgress(book.id, userId) }
                BookWithProgress(book, progress.await())
            }

            _books.postValue(booksWithProgress)
        }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            repository.deleteBookFromCurrentlyReading(book)
            loadCurrentlyReadingBooks(book.userId)
        }
    }
}