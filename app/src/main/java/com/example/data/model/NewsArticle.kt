package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewsArticle(
    val id: String,
    val title: String,
    val description: String,
    val content: String = "",
    val url: String = "",
    val imageUrl: String? = null,
    val sourceName: String = "India News Desk",
    val publishedAt: String = "Just Now",
    val category: String = "India",
    val region: String = "Pan India",
    val language: String = "hi",
    val isBreaking: Boolean = false,
    val isTrending: Boolean = false,
    val isLatest: Boolean = true,
    val isPublished: Boolean = true,
    val isAdminPost: Boolean = false,
    val videoUrl: String? = null,
    val author: String = "GenZ Bharat Team",
    val isSaved: Boolean = false,
    val isRead: Boolean = false,
    val readTimeMinutes: Int = 3,
    val aiSummary: String? = null
)

@JsonClass(generateAdapter = true)
data class VideoNews(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String? = null,
    val category: String = "General",
    val region: String = "Pan India",
    val language: String = "hi",
    val sourceName: String = "GenZ Bharat",
    val author: String = "Admin",
    val isBreaking: Boolean = false,
    val isTrending: Boolean = false,
    val isLatest: Boolean = true,
    val isPublished: Boolean = true,
    val createdAt: String = "Just now"
)

data class AdminStats(
    val totalNews: Int = 0,
    val totalVideos: Int = 0,
    val publishedNews: Int = 0,
    val breakingNews: Int = 0,
    val trendingNews: Int = 0,
    val totalUsers: Int = 0,
    val todayPosts: Int = 0
)

@JsonClass(generateAdapter = true)
data class NewsDataApiResponse(
    val status: String? = null,
    val totalResults: Int? = 0,
    val results: List<NewsDataArticle>? = null
)

@JsonClass(generateAdapter = true)
data class NewsDataArticle(
    @Json(name = "article_id") val articleId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val content: String? = null,
    val link: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "source_id") val sourceId: String? = null,
    @Json(name = "source_name") val sourceName: String? = null,
    val pubDate: String? = null,
    val category: List<String>? = null,
    val country: List<String>? = null,
    val language: String? = null
)
