package com.example.skripsi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.skripsi.data.remote.Book
import java.io.Serializable

@Entity(tableName = "books")

data class BookEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val subtitle: String?,
    val authors: String?,
    val publisher: String?,
    val pages: String?,
    val year: String?,
    val description: String?,
    val image: String?,
    val url: String?,
    val download: String?,
    val status: String,
    val userId: String
) : Serializable
