package com.example.skripsi.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.example.skripsi.data.local.AchievementEntity
import com.example.skripsi.data.local.BookDatabase
import com.example.skripsi.data.local.BookEntity
import com.example.skripsi.data.local.ReadingProgressEntity
import com.example.skripsi.data.remote.ApiConfig
import com.example.skripsi.data.remote.Book
import com.example.skripsi.data.remote.BookResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class Repository(context: Context) {
    private val bookDao = BookDatabase.getDatabase(context).bookDao()
    private val achievementDao = BookDatabase.getDatabase(context).achievementDao()
    private val readingProgressDao = BookDatabase.getDatabase(context).readingProgressDao()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("reading_app_prefs", Context.MODE_PRIVATE)
    private val _recentBooks = MutableLiveData<List<Book>>()
    val recentBooks: LiveData<List<Book>> = _recentBooks
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchRecentBooks() {
        val call = ApiConfig.apiService.getRecentBooks()
        call.enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val books = response.body()?.books ?: emptyList()
                    Log.d("Repository", "Fetched ${books.size} books")
                    _recentBooks.postValue(books)
                } else {
                    _error.postValue("Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Log.e("Repository", "Failed to fetch recent books: ${t.message}")
                _error.postValue("Failure: ${t.message}")
            }
        })
    }

    fun searchBooks(query: String, callback: (Result<List<Book>>) -> Unit) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val call = ApiConfig.apiService.searchBooks(encodedQuery)
        call.enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val books = response.body()?.books ?: emptyList()
                    callback(Result.success(books))
                } else {
                    callback(Result.failure(Exception("Error: ${response.message()}")))
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    fun getBookDetail(bookId: String, callback: (Result<Book>) -> Unit) {
        val call = ApiConfig.apiService.getBookDetail(bookId)
        call.enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful && response.body() != null) {
                    callback(Result.success(response.body()!!))
                } else {
                    callback(Result.failure(Exception("Failed to fetch book details")))
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    suspend fun addBookToCurrentlyReading(book: Book, userId: String) {
        withContext(Dispatchers.IO) {
            val bookEntity = BookEntity(
                id = book.id,
                title = book.title ?: "Unknown Title",
                subtitle = book.subtitle ?: "",
                authors = book.authors ?: "",
                publisher = book.publisher ?: "",
                pages = book.pages ?: "",
                year = book.year ?: "",
                description = book.description ?: "",
                image = book.image ?: "",
                url = book.url ?: "",
                download = book.download ?: "",
                status = "currently_reading",
                userId = userId
            )
            bookDao.insertBook(bookEntity)
        }
    }

    suspend fun addBookToWantToRead(book: Book, userId: String) {
        withContext(Dispatchers.IO) {
            val bookEntity = BookEntity(
                id = book.id,
                title = book.title ?: "Unknown Title",
                subtitle = book.subtitle ?: "",
                authors = book.authors ?: "",
                publisher = book.publisher ?: "",
                pages = book.pages ?: "",
                year = book.year ?: "",
                description = book.description ?: "",
                image = book.image ?: "",
                url = book.url ?: "",
                download = book.download ?: "",
                status = "want_to_read",
                userId = userId
            )
            bookDao.insertBook(bookEntity)
        }
    }

    suspend fun addBookToFinishedReading(book: Book, userId: String) {
        withContext(Dispatchers.IO) {
            val bookEntity = BookEntity(
                id = book.id,
                title = book.title ?: "Unknown Title",
                subtitle = book.subtitle ?: "",
                authors = book.authors ?: "",
                publisher = book.publisher ?: "",
                pages = book.pages ?: "",
                year = book.year ?: "",
                description = book.description ?: "",
                image = book.image ?: "",
                url = book.url ?: "",
                download = book.download ?: "",
                status = "finished_reading",
                userId = userId
            )
            bookDao.insertBook(bookEntity)
        }
    }

    suspend fun getCurrentlyReadingBooks(userId: String): List<BookEntity> {
        return withContext(Dispatchers.IO) {
            bookDao.getBooksByStatusAndUser("currently_reading", userId)
        }
    }

    suspend fun getWantToReadBooks(userId: String): List<BookEntity> {
        return withContext(Dispatchers.IO) {
            bookDao.getBooksByStatusAndUser("want_to_read", userId)
        }
    }

    suspend fun getFinishedReadingBooks(userId: String): List<BookEntity> {
        return withContext(Dispatchers.IO) {
            bookDao.getBooksByStatusAndUser("finished_reading", userId)
        }
    }

    suspend fun deleteBookFromCurrentlyReading(book: BookEntity) {
        withContext(Dispatchers.IO) {
            bookDao.deleteBook(book)
        }
    }

    suspend fun deleteBookFromWantToRead(book: BookEntity) {
        withContext(Dispatchers.IO) {
            bookDao.deleteBook(book)
        }
    }

    suspend fun deleteBookFromFinishedReading(book: BookEntity) {
        withContext(Dispatchers.IO) {
            bookDao.deleteBook(book)
        }
    }

    suspend fun updateReadingProgress(bookId: String, userId: String, progress: Int) {
        withContext(Dispatchers.IO) {
            val readingProgress = ReadingProgressEntity(
                id = "${bookId}_${userId}",
                bookId = bookId,
                userId = userId,
                progress = progress,
                lastReadTimestamp = System.currentTimeMillis()
            )
            readingProgressDao.insertReadingProgress(readingProgress)
        }
    }

    suspend fun getReadingProgress(bookId: String, userId: String): Int {
        return withContext(Dispatchers.IO) {
            val progress = readingProgressDao.getReadingProgress(bookId, userId)
            progress?.progress ?: 0
        }
    }



    suspend fun saveUserAnnualGoal(userId: String, goal: Int) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putInt("annual_goal_$userId", goal).apply()
        }
    }

    suspend fun getUserAnnualGoal(userId: String): Int {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getInt("annual_goal_$userId", 20) // Default to 20 books
        }
    }

    suspend fun saveAchievement(achievement: AchievementEntity) {
        withContext(Dispatchers.IO) {
            achievementDao.insertAchievement(achievement)
        }
    }

    suspend fun getUserAchievements(userId: String): List<AchievementEntity> {
        return withContext(Dispatchers.IO) {
            achievementDao.getUserAchievements(userId)
        }
    }

    suspend fun getAchievement(userId: String, achievementId: String): AchievementEntity? {
        return withContext(Dispatchers.IO) {
            achievementDao.getAchievement(userId, achievementId)
        }
    }

    suspend fun getNewAchievements(userId: String): List<AchievementEntity> {
        return withContext(Dispatchers.IO) {
            achievementDao.getNewAchievements(userId)
        }
    }

    suspend fun markAchievementAsDisplayed(id: String) {
        withContext(Dispatchers.IO) {
            achievementDao.markAchievementAsDisplayed(id)
        }
    }
}