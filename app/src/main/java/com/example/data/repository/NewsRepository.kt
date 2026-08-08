package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.ArticleDao
import com.example.data.local.ArticleEntity
import com.example.data.local.CachedArticleEntity
import com.example.data.model.NewsArticle
import com.example.data.model.NewsDataArticle
import com.example.data.model.VideoNews
import com.example.data.remote.GeminiApiService
import com.example.data.remote.NewsApiClient
import com.example.data.remote.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class NewsFetchResult(
    val articles: List<NewsArticle>,
    val isOffline: Boolean = false,
    val errorMessage: String? = null
)

class NewsRepository(
    private val articleDao: ArticleDao,
    private val supabaseClient: SupabaseClient = SupabaseClient(),
    private val geminiApiService: GeminiApiService = GeminiApiService()
) {

    val savedArticles: Flow<List<NewsArticle>> = articleDao.getAllSavedArticles().map { list ->
        list.map { it.toNewsArticle() }
    }

    fun isArticleSaved(articleId: String): Flow<Boolean> {
        return articleDao.isArticleSavedFlow(articleId)
    }

    suspend fun getPublishedVideos(): List<VideoNews> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.fetchPublishedVideos()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getIndiaNewsFeed(
        category: String? = null,
        query: String? = null,
        language: String = "en",
        region: String? = null
    ): NewsFetchResult = withContext(Dispatchers.IO) {
        val apiKey = getValidApiKey()
        val apiCategory = mapCategoryForApi(category)
        val apiLanguage = if (language.lowercase().startsWith("hi") || language == "Hindi") "hi" else "en"

        // Fetch published Admin news from Supabase to merge with API news
        val adminNewsList = try {
            supabaseClient.fetchPublishedAdminNews()
        } catch (e: Exception) {
            emptyList()
        }

        var searchQuery: String? = null
        if (!query.isNullOrBlank()) {
            searchQuery = query.trim()
        } else if (!region.isNullOrBlank() && region != "All India" && region != "All Regions" && region != "Pan India") {
            searchQuery = region.trim()
        } else if (category != null && apiCategory == null && category != "All" && category != "Home" && category != "National") {
            searchQuery = category.trim()
        }

        val fetchedApiArticles = mutableListOf<NewsArticle>()

        if (apiKey != null) {
            try {
                val response = NewsApiClient.apiService.getIndiaNews(
                    apiKey = apiKey,
                    country = "in",
                    language = apiLanguage,
                    category = apiCategory,
                    query = searchQuery
                )

                val apiResults = response.results
                if (apiResults != null && apiResults.isNotEmpty()) {
                    val articles = apiResults
                        .filter { !it.title.isNullOrBlank() && (!it.link.isNullOrBlank() || !it.articleId.isNullOrBlank()) }
                        .map { it.toNewsArticle(defaultCategory = category ?: "National") }

                    fetchedApiArticles.addAll(articles)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Combine Admin News and API News
        val combinedArticles = mutableListOf<NewsArticle>()
        val filteredAdminNews = filterArticlesLocally(adminNewsList, category, query, region)
        combinedArticles.addAll(filteredAdminNews)
        combinedArticles.addAll(fetchedApiArticles)

        if (combinedArticles.isNotEmpty()) {
            val deduplicated = deduplicateArticles(combinedArticles)
            // Cache fresh API results in Room DB
            if (fetchedApiArticles.isNotEmpty()) {
                val cachedEntities = deduplicateArticles(fetchedApiArticles).map {
                    CachedArticleEntity.fromNewsArticle(it, language = apiLanguage)
                }
                articleDao.insertCachedArticles(cachedEntities)
            }

            return@withContext NewsFetchResult(
                articles = deduplicated,
                isOffline = false,
                errorMessage = null
            )
        }

        // Room Local Database Cache Fallback
        val cachedFromDb = articleDao.getCachedArticlesByLanguage(apiLanguage)
            .ifEmpty { articleDao.getAllCachedArticlesList() }

        if (cachedFromDb.isNotEmpty() || adminNewsList.isNotEmpty()) {
            val articles = cachedFromDb.map { it.toNewsArticle() }
            val allFallback = adminNewsList + articles
            val filtered = filterArticlesLocally(allFallback, category, query, region)
            val deduplicated = deduplicateArticles(if (filtered.isNotEmpty()) filtered else allFallback)
            val errMsg = if (apiKey == null) {
                "Showing cached news & GenZ Bharat publications."
            } else {
                "Showing cached stories (NewsData.io live feed currently unreachable)."
            }
            return@withContext NewsFetchResult(
                articles = deduplicated,
                isOffline = true,
                errorMessage = errMsg
            )
        }

        // No API results and no cached DB entries
        val finalErrMsg = if (apiKey == null) {
            "NewsData.io API key missing. Add NEWSDATA_API_KEY in AI Studio Secrets panel."
        } else {
            "Unable to connect to NewsData.io. Please check your internet connection or API quota."
        }

        return@withContext NewsFetchResult(
            articles = emptyList(),
            isOffline = true,
            errorMessage = finalErrMsg
        )
    }

    private fun filterArticlesLocally(
        articles: List<NewsArticle>,
        category: String?,
        query: String?,
        region: String?
    ): List<NewsArticle> {
        var result = articles
        if (!category.isNullOrBlank() && category != "All" && category != "Home") {
            result = result.filter {
                it.category.equals(category, ignoreCase = true) ||
                it.region.equals(category, ignoreCase = true)
            }
        }

        if (!region.isNullOrBlank() && region != "All India" && region != "All Regions") {
            result = result.filter {
                it.region.contains(region, ignoreCase = true) ||
                it.title.contains(region, ignoreCase = true) ||
                it.description.contains(region, ignoreCase = true)
            }
        }

        if (!query.isNullOrBlank()) {
            val q = query.lowercase().trim()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.sourceName.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }
        }
        return result
    }

    private fun deduplicateArticles(articles: List<NewsArticle>): List<NewsArticle> {
        return articles
            .distinctBy { it.id.trim().lowercase() }
            .distinctBy { if (it.url.isNotBlank()) it.url.trim().lowercase() else it.title.trim().lowercase() }
    }

    private fun mapCategoryForApi(category: String?): String? {
        if (category.isNullOrBlank() || category == "All" || category == "Home") return null
        return when (category.lowercase().trim()) {
            "national", "top" -> "top"
            "politics" -> "politics"
            "business" -> "business"
            "sports" -> "sports"
            "technology", "tech" -> "technology"
            "entertainment" -> "entertainment"
            "health" -> "health"
            "education" -> "education"
            "science" -> "science"
            "world" -> "world"
            else -> null
        }
    }

    suspend fun toggleSaveArticle(article: NewsArticle) = withContext(Dispatchers.IO) {
        val isSaved = articleDao.isArticleSaved(article.id)
        if (isSaved) {
            articleDao.deleteArticleById(article.id)
        } else {
            articleDao.saveArticle(ArticleEntity.fromNewsArticle(article))
        }
    }

    suspend fun generateSummary(article: NewsArticle, language: String = "hi"): String {
        val summary = geminiApiService.generateAiSummary(article, language)
        if (articleDao.isArticleSaved(article.id)) {
            articleDao.updateAiSummary(article.id, summary)
        }
        return summary
    }

    suspend fun translateArticle(article: NewsArticle, language: String): String {
        return geminiApiService.translateArticle(article, language)
    }

    suspend fun askAiQuestion(article: NewsArticle, question: String, language: String = "hi"): String {
        return geminiApiService.askAiQuestion(article, question, language)
    }

    private fun getValidApiKey(): String? {
        var key = try {
            BuildConfig.NEWSDATA_API_KEY
        } catch (e: Throwable) {
            null
        }
        if (key.isNullOrBlank() || key == "MY_NEWSDATA_API_KEY" || key.contains("YOUR_KEY")) {
            key = System.getenv("NEWSDATA_API_KEY")
        }
        if (key.isNullOrBlank() || key == "MY_NEWSDATA_API_KEY" || key.contains("YOUR_KEY")) {
            return null
        }
        return key
    }

    private fun NewsDataArticle.toNewsArticle(defaultCategory: String = "National"): NewsArticle {
        val idStr = articleId?.ifBlank { null } ?: (link?.ifBlank { null } ?: title?.hashCode()?.toString() ?: System.currentTimeMillis().toString())
        val rawCat = category?.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: defaultCategory
        val src = sourceName ?: sourceId?.replaceFirstChar { it.uppercase() } ?: "News Source"
        val pub = pubDate ?: "Recently"
        val isBreak = category?.contains("top") == true || defaultCategory.equals("national", ignoreCase = true)

        val cleanDesc = description?.ifBlank { null } ?: content?.ifBlank { null } ?: title ?: "Read full story."
        val cleanContent = content?.ifBlank { null } ?: cleanDesc

        return NewsArticle(
            id = idStr,
            title = title ?: "India News Update",
            description = cleanDesc,
            content = cleanContent,
            url = link ?: "",
            imageUrl = imageUrl?.ifBlank { null },
            sourceName = src,
            publishedAt = pub,
            category = rawCat,
            region = if (country?.contains("india") == true || country?.contains("in") == true) "National" else "Pan India",
            isBreaking = isBreak,
            readTimeMinutes = 3,
            aiSummary = null
        )
    }
}

