package com.example.skripsi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val userId: String,
    val progress: Int,
    val lastReadTimestamp: Long
)