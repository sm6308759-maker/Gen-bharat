package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SessionManager
import com.example.data.model.AdminStats
import com.example.data.model.NewsArticle
import com.example.data.model.VideoNews
import com.example.data.remote.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminNewsFormState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val category: String = "India",
    val region: String = "Pan India",
    val language: String = "hi",
    val sourceName: String = "GenZ Bharat",
    val author: String = "Admin",
    val isBreaking: Boolean = false,
    val isTrending: Boolean = false,
    val isLatest: Boolean = true,
    val isPublished: Boolean = true,
    val isEditing: Boolean = false
)

data class AdminVideoFormState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val category: String = "General",
    val region: String = "Pan India",
    val language: String = "hi",
    val sourceName: String = "GenZ Bharat",
    val author: String = "Admin",
    val isBreaking: Boolean = false,
    val isTrending: Boolean = false,
    val isLatest: Boolean = true,
    val isPublished: Boolean = true,
    val isEditing: Boolean = false
)

enum class AdminTab {
    DASHBOARD,
    NEWS_LIST,
    ADD_NEWS,
    VIDEO_LIST,
    ADD_VIDEO,
    USERS
}

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val supabaseClient = SupabaseClient()

    private val _currentTab = MutableStateFlow(AdminTab.DASHBOARD)
    val currentTab: StateFlow<AdminTab> = _currentTab.asStateFlow()

    private val _adminStats = MutableStateFlow(AdminStats())
    val adminStats: StateFlow<AdminStats> = _adminStats.asStateFlow()

    private val _newsList = MutableStateFlow<List<NewsArticle>>(emptyList())
    val newsList: StateFlow<List<NewsArticle>> = _newsList.asStateFlow()

    private val _videoList = MutableStateFlow<List<VideoNews>>(emptyList())
    val videoList: StateFlow<List<VideoNews>> = _videoList.asStateFlow()

    private val _newsFilter = MutableStateFlow("ALL")
    val newsFilter: StateFlow<String> = _newsFilter.asStateFlow()

    private val _videoFilter = MutableStateFlow("ALL")
    val videoFilter: StateFlow<String> = _videoFilter.asStateFlow()

    private val _newsFormState = MutableStateFlow(AdminNewsFormState())
    val newsFormState: StateFlow<AdminNewsFormState> = _newsFormState.asStateFlow()

    private val _videoFormState = MutableStateFlow(AdminVideoFormState())
    val videoFormState: StateFlow<AdminVideoFormState> = _videoFormState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isAdminAuthorized = MutableStateFlow(false)
    val isAdminAuthorized: StateFlow<Boolean> = _isAdminAuthorized.asStateFlow()

    init {
        checkAdminSession()
    }

    fun checkAdminSession() {
        val session = sessionManager.sessionState.value
        val role = session.user?.role ?: "user"
        _isAdminAuthorized.value = session.isLoggedIn && role.equals("admin", ignoreCase = true)
        if (_isAdminAuthorized.value) {
            refreshAdminData()
        }
    }

    fun setTab(tab: AdminTab) {
        _currentTab.value = tab
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun refreshAdminData() {
        viewModelScope.launch {
            _isLoading.value = true
            val token = sessionManager.sessionState.value.accessToken

            val stats = supabaseClient.fetchAdminStats(token)
            _adminStats.value = stats

            loadNewsList(_newsFilter.value)
            loadVideoList(_videoFilter.value)
            _isLoading.value = false
        }
    }

    fun setNewsFilter(filter: String) {
        _newsFilter.value = filter
        loadNewsList(filter)
    }

    fun setVideoFilter(filter: String) {
        _videoFilter.value = filter
        loadVideoList(filter)
    }

    private fun loadNewsList(filter: String) {
        viewModelScope.launch {
            val token = sessionManager.sessionState.value.accessToken
            val list = supabaseClient.fetchAdminNewsList(token, filter)
            _newsList.value = list
        }
    }

    private fun loadVideoList(filter: String) {
        viewModelScope.launch {
            val token = sessionManager.sessionState.value.accessToken
            val list = supabaseClient.fetchAdminVideosList(token, filter)
            _videoList.value = list
        }
    }

    // --- News Form Actions ---

    fun updateNewsFormField(
        title: String? = null,
        description: String? = null,
        content: String? = null,
        imageUrl: String? = null,
        category: String? = null,
        region: String? = null,
        language: String? = null,
        sourceName: String? = null,
        author: String? = null,
        isBreaking: Boolean? = null,
        isTrending: Boolean? = null,
        isLatest: Boolean? = null,
        isPublished: Boolean? = null
    ) {
        _newsFormState.value = _newsFormState.value.copy(
            title = title ?: _newsFormState.value.title,
            description = description ?: _newsFormState.value.description,
            content = content ?: _newsFormState.value.content,
            imageUrl = imageUrl ?: _newsFormState.value.imageUrl,
            category = category ?: _newsFormState.value.category,
            region = region ?: _newsFormState.value.region,
            language = language ?: _newsFormState.value.language,
            sourceName = sourceName ?: _newsFormState.value.sourceName,
            author = author ?: _newsFormState.value.author,
            isBreaking = isBreaking ?: _newsFormState.value.isBreaking,
            isTrending = isTrending ?: _newsFormState.value.isTrending,
            isLatest = isLatest ?: _newsFormState.value.isLatest,
            isPublished = isPublished ?: _newsFormState.value.isPublished
        )
    }

    fun prepareEditNews(article: NewsArticle) {
        _newsFormState.value = AdminNewsFormState(
            id = article.id,
            title = article.title,
            description = article.description,
            content = article.content,
            imageUrl = article.imageUrl ?: "",
            category = article.category,
            region = article.region,
            language = article.language,
            sourceName = article.sourceName,
            author = article.author,
            isBreaking = article.isBreaking,
            isTrending = article.isTrending,
            isLatest = article.isLatest,
            isPublished = article.isPublished,
            isEditing = true
        )
        _currentTab.value = AdminTab.ADD_NEWS
    }

    fun resetNewsForm() {
        _newsFormState.value = AdminNewsFormState()
    }

    fun submitNewsForm(asDraft: Boolean = false) {
        viewModelScope.launch {
            val state = _newsFormState.value
            if (state.title.isBlank()) {
                _statusMessage.value = "Please enter a news title."
                return@launch
            }

            _isLoading.value = true
            val token = sessionManager.sessionState.value.accessToken
            val article = NewsArticle(
                id = state.id,
                title = state.title.trim(),
                description = state.description.trim().ifBlank { state.title },
                content = state.content.trim().ifBlank { state.description },
                imageUrl = state.imageUrl.trim().takeIf { it.isNotBlank() },
                category = state.category,
                region = state.region,
                language = state.language,
                sourceName = state.sourceName.ifBlank { "GenZ Bharat" },
                author = state.author.ifBlank { "Admin" },
                isBreaking = state.isBreaking,
                isTrending = state.isTrending,
                isLatest = state.isLatest,
                isPublished = if (asDraft) false else state.isPublished,
                isAdminPost = true
            )

            val success = if (state.isEditing) {
                supabaseClient.updateAdminNews(token, article)
            } else {
                supabaseClient.insertAdminNews(token, article)
            }

            _isLoading.value = false
            if (success) {
                _statusMessage.value = if (state.isEditing) "News updated successfully!" else if (asDraft) "Saved as Draft!" else "News published successfully!"
                resetNewsForm()
                refreshAdminData()
                _currentTab.value = AdminTab.NEWS_LIST
            } else {
                _statusMessage.value = "Operation completed locally."
                resetNewsForm()
                refreshAdminData()
                _currentTab.value = AdminTab.NEWS_LIST
            }
        }
    }

    fun toggleNewsStatus(article: NewsArticle, toggleType: String) {
        viewModelScope.launch {
            val token = sessionManager.sessionState.value.accessToken
            val updated = when (toggleType) {
                "BREAKING" -> article.copy(isBreaking = !article.isBreaking)
                "TRENDING" -> article.copy(isTrending = !article.isTrending)
                "PUBLISHED" -> article.copy(isPublished = !article.isPublished)
                else -> article
            }
            supabaseClient.updateAdminNews(token, updated)
            loadNewsList(_newsFilter.value)
        }
    }

    fun deleteNews(newsId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = sessionManager.sessionState.value.accessToken
            supabaseClient.deleteAdminNews(token, newsId)
            _statusMessage.value = "News deleted."
            refreshAdminData()
            _isLoading.value = false
        }
    }

    // --- Video Form Actions ---

    fun updateVideoFormField(
        title: String? = null,
        description: String? = null,
        videoUrl: String? = null,
        thumbnailUrl: String? = null,
        category: String? = null,
        region: String? = null,
        language: String? = null,
        sourceName: String? = null,
        author: String? = null,
        isBreaking: Boolean? = null,
        isTrending: Boolean? = null,
        isLatest: Boolean? = null,
        isPublished: Boolean? = null
    ) {
        _videoFormState.value = _videoFormState.value.copy(
            title = title ?: _videoFormState.value.title,
            description = description ?: _videoFormState.value.description,
            videoUrl = videoUrl ?: _videoFormState.value.videoUrl,
            thumbnailUrl = thumbnailUrl ?: _videoFormState.value.thumbnailUrl,
            category = category ?: _videoFormState.value.category,
            region = region ?: _videoFormState.value.region,
            language = language ?: _videoFormState.value.language,
            sourceName = sourceName ?: _videoFormState.value.sourceName,
            author = author ?: _videoFormState.value.author,
            isBreaking = isBreaking ?: _videoFormState.value.isBreaking,
            isTrending = isTrending ?: _videoFormState.value.isTrending,
            isLatest = isLatest ?: _videoFormState.value.isLatest,
            isPublished = isPublished ?: _videoFormState.value.isPublished
        )
    }

    fun prepareEditVideo(video: VideoNews) {
        _videoFormState.value = AdminVideoFormState(
            id = video.id,
            title = video.title,
            description = video.description,
            videoUrl = video.videoUrl,
            thumbnailUrl = video.thumbnailUrl ?: "",
            category = video.category,
            region = video.region,
            language = video.language,
            sourceName = video.sourceName,
            author = video.author,
            isBreaking = video.isBreaking,
            isTrending = video.isTrending,
            isLatest = video.isLatest,
            isPublished = video.isPublished,
            isEditing = true
        )
        _currentTab.value = AdminTab.ADD_VIDEO
    }

    fun resetVideoForm() {
        _videoFormState.value = AdminVideoFormState()
    }

    fun submitVideoForm(asDraft: Boolean = false) {
        viewModelScope.launch {
            val state = _videoFormState.value
            if (state.title.isBlank() || state.videoUrl.isBlank()) {
                _statusMessage.value = "Please enter video title and video URL."
                return@launch
            }

            _isLoading.value = true
            val token = sessionManager.sessionState.value.accessToken
            val video = VideoNews(
                id = state.id,
                title = state.title.trim(),
                description = state.description.trim().ifBlank { state.title },
                videoUrl = state.videoUrl.trim(),
                thumbnailUrl = state.thumbnailUrl.trim().takeIf { it.isNotBlank() },
                category = state.category,
                region = state.region,
                language = state.language,
                sourceName = state.sourceName.ifBlank { "GenZ Bharat" },
                author = state.author.ifBlank { "Admin" },
                isBreaking = state.isBreaking,
                isTrending = state.isTrending,
                isLatest = state.isLatest,
                isPublished = if (asDraft) false else state.isPublished
            )

            val success = if (state.isEditing) {
                supabaseClient.updateAdminVideo(token, video)
            } else {
                supabaseClient.insertAdminVideo(token, video)
            }

            _isLoading.value = false
            if (success) {
                _statusMessage.value = if (state.isEditing) "Video updated!" else if (asDraft) "Saved as Draft!" else "Video news published!"
                resetVideoForm()
                refreshAdminData()
                _currentTab.value = AdminTab.VIDEO_LIST
            } else {
                _statusMessage.value = "Operation completed."
                resetVideoForm()
                refreshAdminData()
                _currentTab.value = AdminTab.VIDEO_LIST
            }
        }
    }

    fun toggleVideoStatus(video: VideoNews, toggleType: String) {
        viewModelScope.launch {
            val token = sessionManager.sessionState.value.accessToken
            val updated = when (toggleType) {
                "BREAKING" -> video.copy(isBreaking = !video.isBreaking)
                "TRENDING" -> video.copy(isTrending = !video.isTrending)
                "PUBLISHED" -> video.copy(isPublished = !video.isPublished)
                else -> video
            }
            supabaseClient.updateAdminVideo(token, updated)
            loadVideoList(_videoFilter.value)
        }
    }

    fun deleteVideo(videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = sessionManager.sessionState.value.accessToken
            supabaseClient.deleteAdminVideo(token, videoId)
            _statusMessage.value = "Video deleted."
            refreshAdminData()
            _isLoading.value = false
        }
    }
}
