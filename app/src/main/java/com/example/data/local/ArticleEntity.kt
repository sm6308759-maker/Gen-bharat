package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.NewsArticle

@Entity(tableName = "saved_articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val content: String,
    val url: String,
    val imageUrl: String?,
    val sourceName: String,
    val publishedAt: String,
    val category: String,
    val region: String,
    val isBreaking: Boolean,
    val readTimeMinutes: Int,
    val aiSummary: String?,
    val savedTimestamp: Long = System.currentTimeMillis()
) {
    fun toNewsArticle(): NewsArticle {
        return NewsArticle(
            id = id,
            title = title,
            description = description,
            content = content,
            url = url,
            imageUrl = imageUrl,
            sourceName = sourceName,
            publishedAt = publishedAt,
            category = category,
            region = region,
            isBreaking = isBreaking,
            isSaved = true,
            isRead = false,
            readTimeMinutes = readTimeMinutes,
            aiSummary = aiSummary
        )
    }

    companion object {
        fun fromNewsArticle(article: NewsArticle): ArticleEntity {
            return ArticleEntity(
                id = article.id,
                title = article.title,
                description = article.description,
                content = article.content,
                url = article.url,
                imageUrl = article.imageUrl,
                sourceName = article.sourceName,
                publishedAt = article.publishedAt,
                category = article.category,
                region = article.region,
                isBreaking = article.isBreaking,
                readTimeMinutes = article.readTimeMinutes,
                aiSummary = article.aiSummary
            )
        }
    }
}
