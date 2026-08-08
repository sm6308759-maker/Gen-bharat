package com.example.data.remote

import com.example.data.model.NewsDataApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("1/news")
    suspend fun getIndiaNews(
        @Query("apikey") apiKey: String,
        @Query("country") country: String = "in",
        @Query("language") language: String = "en",
        @Query("category") category: String? = null,
        @Query("q") query: String? = null
    ): NewsDataApiResponse
}
