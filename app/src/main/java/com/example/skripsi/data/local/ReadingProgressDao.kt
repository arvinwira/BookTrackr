package com.example.skripsi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReadingProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingProgress(readingProgress: ReadingProgressEntity)

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId AND userId = :userId")
    suspend fun getReadingProgress(bookId: String, userId: String): ReadingProgressEntity?

}