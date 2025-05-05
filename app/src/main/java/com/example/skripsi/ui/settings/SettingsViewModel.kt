package com.example.skripsi.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.skripsi.data.repository.AuthRepository

class SettingsViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val _email = MutableLiveData<String?>()

    val email: LiveData<String?> get() = _email

    fun fetchEmail() {
        _email.value = authRepository.getCurrentUser()?.email
    }

    fun logout() {
        authRepository.logout()
    }
}