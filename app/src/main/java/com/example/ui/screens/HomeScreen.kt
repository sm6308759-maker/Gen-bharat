package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.CircleShape
import com.example.ui.theme.TextPrimary
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.NewsArticle
import com.example.data.model.VideoNews
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ArticleItemCard
import com.example.ui.components.BreakingTickerBar
import com.example.ui.components.CategoryFilterChips
import com.example.ui.components.EmptyStateType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NewsHeaderCard
import com.example.ui.components.SkeletonNewsCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.TextMuted

@Composable
fun HomeScreen(
    newsArticles: List<NewsArticle>,
    breakingArticles: List<NewsArticle>,
    savedArticles: List<NewsArticle>,
    videoList: List<VideoNews> = emptyList(),
    selectedCategory: String,
    searchQuery: String,
    newsLanguage: String,
    isLoading: Boolean,
    isOfflineMode: Boolean = false,
    errorMessage: String?,
    onCategorySelected: (String) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onRetryClick: () -> Unit,
    onArticleClick: (NewsArticle) -> Unit,
    onAiClick: (NewsArticle) -> Unit,
    onSaveClick: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("National", "Politics", "Business", "Sports", "Technology", "Entertainment", "Health", "Education", "Science", "World")
    val savedIds = savedArticles.map { it.id }.toSet()
    var activePlayingVideo by remember { mutableStateOf<VideoNews?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        // Search & Language Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search India news or topic...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = DeepNavy
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = TextMuted
                            )
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("home_search_bar"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Language Switcher Chips (English / Hindi)
            Row(
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = newsLanguage == "en",
                    onClick = { onLanguageSelected("en") },
                    label = { Text("EN", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepNavy,
                        selectedLabelColor = PureWhite,
                        containerColor = SoftGrayBg,
                        labelColor = TextMuted
                    ),
                    border = null,
                    modifier = Modifier.testTag("lang_en_chip")
                )
                FilterChip(
                    selected = newsLanguage == "hi",
                    onClick = { onLanguageSelected("hi") },
                    label = { Text("हिंदी", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepNavy,
                        selectedLabelColor = PureWhite,
                        containerColor = SoftGrayBg,
                        labelColor = TextMuted
                    ),
                    border = null,
                    modifier = Modifier.testTag("lang_hi_chip")
                )
            }
        }

        // Category Filter Chips
        CategoryFilterChips(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )

        // Offline / Cached Mode Indicator
        if (isOfflineMode) {
            Surface(
                color = SoftGrayBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Offline / Cached Stories",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaffronPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Showing locally cached feed",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRetryClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = DeepNavy
                        )
                    }
                }
            }
        }

        // Breaking Ticker Bar
        BreakingTickerBar(
            breakingArticles = breakingArticles,
            onArticleClick = onArticleClick
        )

        // API Error Banner if present
        if (errorMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRetryClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        if (isLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                items(5) {
                    SkeletonNewsCard()
                }
            }
        } else if (newsArticles.isEmpty()) {
            EmptyStateView(
                type = EmptyStateType.NO_NEWS,
                title = "No stories found for '$selectedCategory'",
                message = "Pull down to refresh or try selecting another news category.",
                onActionClick = onRetryClick,
                actionButtonText = "Refresh Feed"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_news_list")
            ) {
                // Top Hero Article
                item {
                    val heroArticle = newsArticles.first()
                    NewsHeaderCard(
                        article = heroArticle,
                        isSaved = savedIds.contains(heroArticle.id),
                        onArticleClick = onArticleClick,
                        onAiClick = onAiClick,
                        onSaveClick = onSaveClick
                    )
                }

                // GenZ Bharat Video Section
                if (videoList.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(SaffronPrimary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (newsLanguage == "hi") "वीडियो समाचार (VIDEO NEWS)" else "GENZ BHARAT VIDEO NEWS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = DeepNavy,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                            ) {
                                items(videoList, key = { it.id }) { video ->
                                    Card(
                                        modifier = Modifier
                                            .width(200.dp)
                                            .clickable { activePlayingVideo = video },
                                        colors = CardDefaults.cardColors(containerColor = SoftGrayBg),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(115.dp)
                                                    .background(DeepNavy),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!video.thumbnailUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = video.thumbnailUrl,
                                                        contentDescription = video.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(SaffronPrimary, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Play",
                                                        tint = PureWhite,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = video.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = TextPrimary,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "${video.category} • ${video.author}",
                                                    fontSize = 10.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Trending Section (if >= 4 articles)
                if (newsArticles.size >= 4) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFFFFF7ED),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (newsLanguage == "hi") "🔥 ट्रेंडिंग समाचार (TRENDING IN BHARAT)" else "🔥 TRENDING IN BHARAT",
                                        color = SaffronPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val trendingArticles = newsArticles.subList(1, minOf(4, newsArticles.size))
                            trendingArticles.forEachIndexed { index, article ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .clickable { onArticleClick(article) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = SoftGrayBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Real Article Image / Thumbnail Box with Rank Badge
                                        Box(
                                            modifier = Modifier.size(width = 88.dp, height = 72.dp)
                                        ) {
                                            if (!article.imageUrl.isNullOrEmpty()) {
                                                AsyncImage(
                                                    model = article.imageUrl,
                                                    contentDescription = article.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(RoundedCornerShape(10.dp))
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Newspaper,
                                                        contentDescription = null,
                                                        tint = Color(0xFF94A3B8),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }

                                            // Small rank badge overlay
                                            Surface(
                                                color = SaffronPrimary,
                                                shape = RoundedCornerShape(bottomEnd = 6.dp, topStart = 10.dp),
                                                modifier = Modifier.align(Alignment.TopStart)
                                            ) {
                                                Text(
                                                    text = "#${index + 1}",
                                                    color = PureWhite,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = Color(0xFFFFF7ED),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = article.category,
                                                        color = SaffronPrimary,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = article.sourceName,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = article.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = DeepNavy,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = article.publishedAt,
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (newsLanguage == "hi") "ताज़ा समाचार (Live Feed)" else "Latest News Stories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Remaining Articles List
                val remainingArticles = if (newsArticles.size >= 4) newsArticles.drop(4) else newsArticles.drop(1)
                itemsIndexed(remainingArticles) { _, article ->
                    ArticleItemCard(
                        article = article,
                        isSaved = savedIds.contains(article.id),
                        onArticleClick = onArticleClick,
                        onAiClick = onAiClick,
                        onSaveClick = onSaveClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Video Player Dialog Modal
        activePlayingVideo?.let { video ->
            Dialog(
                onDismissRequest = { activePlayingVideo = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(16.dp)),
                    color = PureWhite
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GenZ Bharat Video News",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DeepNavy
                            )
                            IconButton(onClick = { activePlayingVideo = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = DeepNavy)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DeepNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!video.thumbnailUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = video.thumbnailUrl,
                                    contentDescription = video.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(SaffronPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Playing",
                                        tint = PureWhite,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = DeepNavy.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "▶ Playing Video Stream",
                                        color = PureWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = video.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = video.description,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Source: ${video.sourceName} • Author: ${video.author}",
                            fontSize = 11.sp,
                            color = SaffronPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
