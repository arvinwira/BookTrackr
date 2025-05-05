package com.example.skripsi.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.skripsi.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    fun register(email: String, password: String) = liveData(Dispatchers.IO) {
        try {
            val result = authRepository.register(email, password)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun login(email: String, password: String) = liveData(Dispatchers.IO) {
        try {
            val result = authRepository.login(email, password)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

}