package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM saved_articles ORDER BY savedTimestamp DESC")
    fun getAllSavedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM saved_articles ORDER BY savedTimestamp DESC")
    suspend fun getSavedArticlesList(): List<ArticleEntity>

    @Query("DELETE FROM saved_articles")
    suspend fun clearAllSavedArticles()

    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE id = :id)")
    fun isArticleSavedFlow(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE id = :id)")
    suspend fun isArticleSaved(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArticle(article: ArticleEntity)

    @Query("DELETE FROM saved_articles WHERE id = :id")
    suspend fun deleteArticleById(id: String)

    @Query("UPDATE saved_articles SET aiSummary = :summary WHERE id = :id")
    suspend fun updateAiSummary(id: String, summary: String)

    // Cached Articles Operations for Offline / API Fallback
    @Query("SELECT * FROM cached_articles ORDER BY cachedTimestamp DESC")
    suspend fun getAllCachedArticlesList(): List<CachedArticleEntity>

    @Query("SELECT * FROM cached_articles WHERE language = :language ORDER BY cachedTimestamp DESC")
    suspend fun getCachedArticlesByLanguage(language: String): List<CachedArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedArticles(articles: List<CachedArticleEntity>)

    @Query("DELETE FROM cached_articles")
    suspend fun clearCachedArticles()
}

