package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class SupabaseUser(
    val id: String,
    val email: String,
    val fullName: String,
    val createdAt: String,
    val role: String = "user",
    val accessToken: String = "",
    val refreshToken: String = ""
)

data class UserSession(
    val isLoggedIn: Boolean = false,
    val isGuest: Boolean = false,
    val user: SupabaseUser? = null,
    val accessToken: String = "",
    val refreshToken: String = ""
)

data class SupabaseBookmark(
    val id: String = "",
    val userId: String,
    val articleId: String,
    val articleTitle: String,
    val articleDesc: String? = null,
    val articleContent: String? = null,
    val articleUrl: String? = null,
    val imageUrl: String? = null,
    val sourceName: String? = null,
    val publishedAt: String? = null,
    val category: String = "National",
    val createdAt: String = ""
)

data class UserPreferences(
    val userId: String,
    val newsLanguage: String = "en",
    val notificationsEnabled: Boolean = true,
    val theme: String = "light",
    val updatedAt: String = ""
)

data class AuthResponse(
    val success: Boolean,
    val user: SupabaseUser? = null,
    val errorMessage: String? = null
)
