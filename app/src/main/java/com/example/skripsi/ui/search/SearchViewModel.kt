package com.example.skripsi.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skripsi.data.remote.Book
import com.example.skripsi.data.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: Repository) : ViewModel() {

    private val _searchResults = MutableLiveData<List<Book>>()
    val searchResults: LiveData<List<Book>> = _searchResults

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun searchBooks(query: String) {
        _isLoading.postValue(true)

        viewModelScope.launch(Dispatchers.IO) {
            repository.searchBooks(query) { result ->
                _isLoading.postValue(false)

                result.onSuccess { books ->
                    if (books.isEmpty()) {
                        _searchResults.postValue(emptyList())
                    } else {
                        _searchResults.postValue(books)
                    }
                }.onFailure { throwable ->
                    _error.postValue(throwable.message ?: "Unknown error")
                }
            }
        }
    }
}