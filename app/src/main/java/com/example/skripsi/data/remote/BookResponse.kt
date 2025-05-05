package com.example.skripsi.data.remote

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BookResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total") val total: Int,
    @SerializedName("books") val books: List<Book>
)

data class Book(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("authors") val authors: String?,
    @SerializedName("publisher") val publisher: String?,
    @SerializedName("pages") val pages: String?,
    @SerializedName("year") val year: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("download") val download: String?
) : Serializable

