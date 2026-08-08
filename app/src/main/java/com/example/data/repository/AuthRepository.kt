package com.example.data.repository

import com.example.data.local.ArticleDao
import com.example.data.local.ArticleEntity
import com.example.data.local.SessionManager
import com.example.data.model.AuthResponse
import com.example.data.model.NewsArticle
import com.example.data.model.UserSession
import com.example.data.remote.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class AuthRepository(
    private val sessionManager: SessionManager,
    private val supabaseClient: SupabaseClient = SupabaseClient(),
    private val articleDao: ArticleDao
) {

    val sessionState: StateFlow<UserSession> = sessionManager.sessionState

    init {
        val customUrl = sessionManager.getCustomSupabaseUrl()
        val customKey = sessionManager.getCustomSupabaseKey()
        supabaseClient.updateCustomConfig(customUrl, customKey)
    }

    suspend fun signUp(email: String, password: String, fullName: String): AuthResponse = withContext(Dispatchers.IO) {
        val response = supabaseClient.signUp(email, password, fullName)
        if (response.success && response.user != null) {
            sessionManager.saveSession(
                user = response.user,
                accessToken = response.user.accessToken,
                refreshToken = response.user.refreshToken
            )
            // Auto-create user profile on Supabase Cloud
            supabaseClient.upsertProfile(
                accessToken = response.user.accessToken,
                userId = response.user.id,
                email = response.user.email,
                fullName = fullName
            )
            // Initial sync of existing local bookmarks to Supabase Cloud
            syncLocalBookmarksToCloud()
        }
        response
    }

    suspend fun signIn(email: String, password: String): AuthResponse = withContext(Dispatchers.IO) {
        val response = supabaseClient.signIn(email, password)
        if (response.success && response.user != null) {
            var updatedUser = response.user
            val role = supabaseClient.fetchUserRole(response.user.accessToken, response.user.id)
            // Ensure profile exists or fetch updated profile name
            val profileName = supabaseClient.fetchProfileName(response.user.accessToken, response.user.id)
            if (!profileName.isNullOrBlank()) {
                updatedUser = updatedUser.copy(fullName = profileName, role = role)
            } else {
                updatedUser = updatedUser.copy(role = role)
                supabaseClient.upsertProfile(
                    accessToken = response.user.accessToken,
                    userId = response.user.id,
                    email = response.user.email,
                    fullName = response.user.fullName
                )
            }

            sessionManager.saveSession(
                user = updatedUser,
                accessToken = updatedUser.accessToken,
                refreshToken = updatedUser.refreshToken
            )
            // Sync local guest bookmarks to cloud
            syncLocalBookmarksToCloud()
            // Fetch cloud bookmarks and merge with Room local bookmarks
            syncCloudBookmarksToLocal()
        }
        response
    }

    suspend fun resetPassword(email: String): AuthResponse = withContext(Dispatchers.IO) {
        supabaseClient.resetPassword(email)
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        val currentSession = sessionState.value
        if (currentSession.accessToken.isNotBlank()) {
            supabaseClient.signOut(currentSession.accessToken)
        }
        articleDao.clearAllSavedArticles()
        sessionManager.clearSession()
    }

    suspend fun updateProfileName(fullName: String): Boolean = withContext(Dispatchers.IO) {
        val session = sessionState.value
        if (session.isLoggedIn && session.user != null && session.accessToken.isNotBlank()) {
            val success = supabaseClient.updateProfile(
                accessToken = session.accessToken,
                userId = session.user.id,
                fullName = fullName
            )
            if (success) {
                sessionManager.updateUserProfileName(fullName)
                return@withContext true
            }
        }
        false
    }

    suspend fun deleteAccount(): Boolean = withContext(Dispatchers.IO) {
        val session = sessionState.value
        if (session.isLoggedIn && session.user != null && session.accessToken.isNotBlank()) {
            val success = supabaseClient.deleteAccountData(
                accessToken = session.accessToken,
                userId = session.user.id
            )
            articleDao.clearAllSavedArticles()
            sessionManager.clearSession()
            return@withContext success
        }
        false
    }

    fun continueAsGuest() {
        sessionManager.setGuestMode(true)
    }

    suspend fun syncBookmarkAction(article: NewsArticle, isSaving: Boolean) = withContext(Dispatchers.IO) {
        val session = sessionState.value
        if (session.isLoggedIn && session.user != null && session.accessToken.isNotBlank()) {
            if (isSaving) {
                supabaseClient.syncBookmarkToCloud(
                    accessToken = session.accessToken,
                    userId = session.user.id,
                    article = article
                )
            } else {
                supabaseClient.deleteBookmarkFromCloud(
                    accessToken = session.accessToken,
                    userId = session.user.id,
                    articleId = article.id
                )
            }
        }
    }

    private suspend fun syncCloudBookmarksToLocal() = withContext(Dispatchers.IO) {
        val session = sessionState.value
        if (session.isLoggedIn && session.user != null && session.accessToken.isNotBlank()) {
            val cloudArticles = supabaseClient.fetchCloudBookmarks(session.accessToken, session.user.id)
            for (article in cloudArticles) {
                if (!articleDao.isArticleSaved(article.id)) {
                    articleDao.saveArticle(ArticleEntity.fromNewsArticle(article))
                }
            }
        }
    }

    private suspend fun syncLocalBookmarksToCloud() = withContext(Dispatchers.IO) {
        val session = sessionState.value
        if (session.isLoggedIn && session.user != null && session.accessToken.isNotBlank()) {
            val localArticles = articleDao.getSavedArticlesList()
            for (entity in localArticles) {
                supabaseClient.syncBookmarkToCloud(
                    accessToken = session.accessToken,
                    userId = session.user.id,
                    article = entity.toNewsArticle()
                )
            }
        }
    }

    suspend fun savePreferences(newsLanguage: String, notificationsEnabled: Boolean) = withContext(Dispatchers.IO) {
        sessionManager.setNewsLanguage(newsLanguage)
        sessionManager.setNotificationsEnabled(notificationsEnabled)

        val session = sessionState.value
        if (session.isLoggedIn && session.user != null && session.accessToken.isNotBlank()) {
            supabaseClient.updateUserPreferences(
                accessToken = session.accessToken,
                userId = session.user.id,
                newsLanguage = newsLanguage,
                notificationsEnabled = notificationsEnabled
            )
        }
    }

    fun saveCustomSupabaseConfig(url: String, key: String) {
        sessionManager.saveCustomSupabaseConfig(url, key)
        supabaseClient.updateCustomConfig(url, key)
    }
}
