package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.SupabaseUser
import com.example.data.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _sessionState = MutableStateFlow(loadSession())
    val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

    companion object {
        private const val PREF_NAME = "bharat_pulse_user_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_GUEST = "is_guest"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_CREATED_AT = "user_created_at"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_NEWS_LANGUAGE = "news_language"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_CUSTOM_SUPABASE_URL = "custom_supabase_url"
        private const val KEY_CUSTOM_SUPABASE_KEY = "custom_supabase_key"
        private const val KEY_USER_ROLE = "user_role"
    }

    fun loadSession(): UserSession {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val isGuest = prefs.getBoolean(KEY_IS_GUEST, false)

        if (!isLoggedIn) {
            return UserSession(isLoggedIn = false, isGuest = isGuest)
        }

        val userId = prefs.getString(KEY_USER_ID, "") ?: ""
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val fullName = prefs.getString(KEY_USER_NAME, "GenZ Bharat Reader") ?: "GenZ Bharat Reader"
        val createdAt = prefs.getString(KEY_USER_CREATED_AT, "2026-08-01") ?: "2026-08-01"
        val role = prefs.getString(KEY_USER_ROLE, "user") ?: "user"
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, "") ?: ""
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""

        val user = SupabaseUser(
            id = userId,
            email = email,
            fullName = fullName,
            createdAt = createdAt,
            role = role,
            accessToken = accessToken,
            refreshToken = refreshToken
        )

        return UserSession(
            isLoggedIn = true,
            isGuest = false,
            user = user,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun saveSession(user: SupabaseUser, accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_IS_GUEST, false)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_NAME, user.fullName)
            putString(KEY_USER_CREATED_AT, user.createdAt)
            putString(KEY_USER_ROLE, user.role)
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
        _sessionState.value = UserSession(
            isLoggedIn = true,
            isGuest = false,
            user = user.copy(accessToken = accessToken, refreshToken = refreshToken),
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun updateUserProfileName(fullName: String) {
        prefs.edit().putString(KEY_USER_NAME, fullName).apply()
        val current = _sessionState.value
        val currentUser = current.user
        if (currentUser != null) {
            val updatedUser = currentUser.copy(fullName = fullName)
            _sessionState.value = current.copy(user = updatedUser)
        }
    }

    fun setGuestMode(isGuest: Boolean) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            putBoolean(KEY_IS_GUEST, isGuest)
            apply()
        }
        _sessionState.value = UserSession(isLoggedIn = false, isGuest = isGuest)
    }

    fun clearSession() {
        val isGuest = prefs.getBoolean(KEY_IS_GUEST, false)
        prefs.edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_USER_CREATED_AT)
            remove(KEY_USER_ROLE)
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            apply()
        }
        _sessionState.value = UserSession(isLoggedIn = false, isGuest = isGuest)
    }

    fun getNewsLanguage(): String {
        return prefs.getString(KEY_NEWS_LANGUAGE, "hi") ?: "hi"
    }

    fun setNewsLanguage(language: String) {
        prefs.edit().putString(KEY_NEWS_LANGUAGE, language).apply()
    }

    fun getNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getCustomSupabaseUrl(): String? = prefs.getString(KEY_CUSTOM_SUPABASE_URL, null)
    fun getCustomSupabaseKey(): String? = prefs.getString(KEY_CUSTOM_SUPABASE_KEY, null)

    fun saveCustomSupabaseConfig(url: String, key: String) {
        val cleanedUrl = com.example.data.remote.SupabaseClient.sanitizeSupabaseUrl(url)
        prefs.edit().apply {
            putString(KEY_CUSTOM_SUPABASE_URL, cleanedUrl)
            putString(KEY_CUSTOM_SUPABASE_KEY, key.trim())
            apply()
        }
    }
}
