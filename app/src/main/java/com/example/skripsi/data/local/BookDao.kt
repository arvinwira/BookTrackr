package com.example.skripsi.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Query("SELECT * FROM books WHERE status = :status AND userId = :userId")
    suspend fun getBooksByStatusAndUser(status: String, userId: String): List<BookEntity>

    @Delete
    suspend fun deleteBook(book: BookEntity)

}


