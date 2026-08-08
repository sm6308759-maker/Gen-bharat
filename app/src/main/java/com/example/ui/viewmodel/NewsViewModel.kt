package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SessionManager
import com.example.data.model.NewsArticle
import com.example.data.model.VideoNews
import com.example.data.repository.AuthRepository
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sessionManager = SessionManager(application)
    private val repository = NewsRepository(db.articleDao())
    private val authRepository = AuthRepository(sessionManager, articleDao = db.articleDao())

    private val _newsList = MutableStateFlow<List<NewsArticle>>(emptyList())
    val newsList: StateFlow<List<NewsArticle>> = _newsList.asStateFlow()

    private val _breakingNewsList = MutableStateFlow<List<NewsArticle>>(emptyList())
    val breakingNewsList: StateFlow<List<NewsArticle>> = _breakingNewsList.asStateFlow()

    private val _videoList = MutableStateFlow<List<VideoNews>>(emptyList())
    val videoList: StateFlow<List<VideoNews>> = _videoList.asStateFlow()

    val savedArticles: StateFlow<List<NewsArticle>> = repository.savedArticles.toStateFlow(emptyList())

    private val _selectedCategory = MutableStateFlow("National")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _newsLanguage = MutableStateFlow(sessionManager.getNewsLanguage())
    val newsLanguage: StateFlow<String> = _newsLanguage.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(sessionManager.getNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var searchJob: Job? = null

    // AI Bottom Sheet & Translation State
    private val _selectedArticleForAi = MutableStateFlow<NewsArticle?>(null)
    val selectedArticleForAi: StateFlow<NewsArticle?> = _selectedArticleForAi.asStateFlow()

    private val _aiSummaryText = MutableStateFlow<String?>(null)
    val aiSummaryText: StateFlow<String?> = _aiSummaryText.asStateFlow()

    private val _isGeneratingAiSummary = MutableStateFlow(false)
    val isGeneratingAiSummary: StateFlow<Boolean> = _isGeneratingAiSummary.asStateFlow()

    private val _selectedTranslationLang = MutableStateFlow("English")
    val selectedTranslationLang: StateFlow<String> = _selectedTranslationLang.asStateFlow()

    private val _translatedContent = MutableStateFlow<String?>(null)
    val translatedContent: StateFlow<String?> = _translatedContent.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _aiChatMessages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiChatMessages: StateFlow<List<Pair<String, String>>> = _aiChatMessages.asStateFlow()

    private val _isAskingAi = MutableStateFlow(false)
    val isAskingAi: StateFlow<Boolean> = _isAskingAi.asStateFlow()

    init {
        loadNewsFeed()
    }

    fun loadNewsFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.getIndiaNewsFeed(
                category = _selectedCategory.value,
                query = _searchQuery.value,
                language = _newsLanguage.value
            )

            _newsList.value = result.articles
            _isOfflineMode.value = result.isOffline
            _errorMessage.value = result.errorMessage

            val videos = repository.getPublishedVideos()
            _videoList.value = videos

            val breaking = result.articles.filter {
                it.isBreaking || it.category.equals("top", ignoreCase = true) || it.category.equals("national", ignoreCase = true)
            }
            _breakingNewsList.value = if (breaking.isNotEmpty()) breaking else result.articles.take(3)
            _isLoading.value = false
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        loadNewsFeed()
    }

    fun setNewsLanguage(languageCode: String) {
        if (_newsLanguage.value != languageCode) {
            _newsLanguage.value = languageCode
            sessionManager.setNewsLanguage(languageCode)
            loadNewsFeed()
            viewModelScope.launch {
                authRepository.savePreferences(languageCode, _notificationsEnabled.value)
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        sessionManager.setNotificationsEnabled(enabled)
        viewModelScope.launch {
            authRepository.savePreferences(_newsLanguage.value, enabled)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            loadNewsFeed()
        }
    }

    fun toggleSaveArticle(article: NewsArticle) {
        viewModelScope.launch {
            val isSaved = savedArticles.value.any { it.id == article.id }
            repository.toggleSaveArticle(article)
            authRepository.syncBookmarkAction(article, isSaving = !isSaved)
        }
    }

    fun openAiSheetForArticle(article: NewsArticle) {
        _selectedArticleForAi.value = article
        _aiSummaryText.value = article.aiSummary
        _translatedContent.value = null
        _selectedTranslationLang.value = "English"
        _aiChatMessages.value = emptyList()

        if (article.aiSummary == null) {
            generateAiSummaryForArticle(article)
        }
    }

    fun closeAiSheet() {
        _selectedArticleForAi.value = null
        _aiSummaryText.value = null
        _translatedContent.value = null
    }

    fun generateAiSummaryForArticle(article: NewsArticle) {
        viewModelScope.launch {
            _isGeneratingAiSummary.value = true
            val summary = repository.generateSummary(article)
            _aiSummaryText.value = summary
            _isGeneratingAiSummary.value = false
        }
    }

    fun translateArticle(language: String) {
        val article = _selectedArticleForAi.value ?: return
        _selectedTranslationLang.value = language
        if (language == "English") {
            _translatedContent.value = null
            return
        }
        viewModelScope.launch {
            _isTranslating.value = true
            val translated = repository.translateArticle(article, language)
            _translatedContent.value = translated
            _isTranslating.value = false
        }
    }

    fun askAiQuestion(question: String) {
        val article = _selectedArticleForAi.value ?: return
        if (question.isBlank()) return
        viewModelScope.launch {
            _isAskingAi.value = true
            val currentList = _aiChatMessages.value.toMutableList()
            val answer = repository.askAiQuestion(article, question)
            currentList.add(Pair(question, answer))
            _aiChatMessages.value = currentList
            _isAskingAi.value = false
        }
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.toStateFlow(initialValue: T): StateFlow<T> {
        val state = MutableStateFlow(initialValue)
        viewModelScope.launch {
            this@toStateFlow.collectLatest { state.value = it }
        }
        return state.asStateFlow()
    }
}
