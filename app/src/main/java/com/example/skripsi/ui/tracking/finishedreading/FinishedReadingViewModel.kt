package com.example.skripsi.ui.tracking.finishedreading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skripsi.data.local.BookEntity
import com.example.skripsi.data.repository.Repository
import kotlinx.coroutines.launch

class FinishedReadingViewModel (private val repository: Repository) : ViewModel(){
    private val _books = MutableLiveData<List<BookEntity>>()
    val books: LiveData<List<BookEntity>> get() = _books

    fun loadFinishedReadingBooks(userId: String){
        viewModelScope.launch {
            _books.postValue(repository.getFinishedReadingBooks(userId))

        }
    }

    fun deleteBook(book: BookEntity){
        viewModelScope.launch{
            repository.deleteBookFromFinishedReading(book)
            loadFinishedReadingBooks(book.userId)
        }
    }


}