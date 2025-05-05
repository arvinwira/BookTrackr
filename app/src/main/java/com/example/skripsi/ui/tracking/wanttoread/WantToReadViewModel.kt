package com.example.skripsi.ui.tracking.wanttoread

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skripsi.data.local.BookEntity
import com.example.skripsi.data.repository.Repository
import kotlinx.coroutines.launch

class WantToReadViewModel(private val repository: Repository) : ViewModel() {

    private val _books = MutableLiveData<List<BookEntity>>()
    val books: LiveData<List<BookEntity>> get() = _books

    fun loadWantToReadBooks(userId: String) {
        viewModelScope.launch {
            val bookList = repository.getWantToReadBooks(userId)
            _books.postValue(bookList)
        }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            repository.deleteBookFromWantToRead(book)
            loadWantToReadBooks(book.userId)
        }
    }
}

