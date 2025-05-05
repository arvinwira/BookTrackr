package com.example.skripsi.ui.bookdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skripsi.data.remote.Book
import com.example.skripsi.data.repository.Repository
import kotlinx.coroutines.launch

class BookDetailViewModel(private val repository: Repository) : ViewModel() {

    private val _book = MutableLiveData<Book>()
    val book: LiveData<Book> = _book

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun setBookData(book: Book) {
        _book.value = book

        if (book.description.isNullOrEmpty()) {
            fetchBookDetails(book.id)
        }
    }

    private fun fetchBookDetails(bookId: String?) {
        if (bookId.isNullOrEmpty()) {
            _errorMessage.postValue("Invalid book ID")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            repository.getBookDetail(bookId) { result ->
                _isLoading.postValue(false)
                result.onSuccess { fullBook ->
                    _book.postValue(fullBook)
                }.onFailure { error ->
                    _errorMessage.postValue("Error fetching details: ${error.message}")
                }
            }
        }
    }

    fun onCurrentlyReadingButtonClicked(book: Book, userId: String) {
        viewModelScope.launch {
            repository.addBookToCurrentlyReading(book, userId)
        }
    }

    fun onWantToReadButtonClicked(book: Book, userId: String) {
        viewModelScope.launch {
            repository.addBookToWantToRead(book, userId)
        }
    }

    fun onFinishedReadingButtonClicked(book: Book, userId: String) {
        viewModelScope.launch {
            repository.addBookToFinishedReading(book, userId)
        }
    }
}