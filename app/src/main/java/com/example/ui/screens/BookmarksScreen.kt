package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsArticle
import com.example.ui.components.ArticleItemCard
import com.example.ui.components.EmptyStateType
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted

@Composable
fun BookmarksScreen(
    savedArticles: List<NewsArticle>,
    onArticleClick: (NewsArticle) -> Unit,
    onAiClick: (NewsArticle) -> Unit,
    onSaveClick: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = if (searchQuery.isBlank()) {
        savedArticles
    } else {
        val q = searchQuery.lowercase().trim()
        savedArticles.filter {
            it.title.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .testTag("bookmarks_screen")
    ) {
        // Search Saved Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search your saved stories...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Saved",
                    tint = DeepNavy
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextMuted
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("bookmarks_search_bar"),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        if (savedArticles.isEmpty()) {
            EmptyStateView(
                type = EmptyStateType.NO_BOOKMARKS
            )
        } else if (filteredList.isEmpty()) {
            EmptyStateView(
                type = EmptyStateType.NO_SEARCH_RESULTS,
                title = "No saved stories match '$searchQuery'",
                message = "Try searching for another keyword or clear the query filter.",
                onActionClick = { searchQuery = "" },
                actionButtonText = "Clear Search Filter"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("bookmarks_news_list")
            ) {
                items(filteredList) { article ->
                    ArticleItemCard(
                        article = article,
                        isSaved = true,
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
    }
}
