package com.example.skripsi.data.remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("recent")
    fun getRecentBooks(): Call<BookResponse>

    @GET("book/{id}")
    fun getBookDetail(@Path("id") id: String): Call<Book>

    @GET("search/{query}")
    fun searchBooks(@Path("query") query: String): Call<BookResponse>
}