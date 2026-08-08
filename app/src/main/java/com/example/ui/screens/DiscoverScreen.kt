package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsArticle
import com.example.ui.components.ArticleItemCard
import com.example.ui.components.EmptyStateType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SkeletonNewsCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.TextMuted

data class CategoryGridItem(val name: String, val icon: ImageVector)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverScreen(
    newsArticles: List<NewsArticle>,
    savedArticles: List<NewsArticle>,
    searchQuery: String,
    isLoading: Boolean,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onArticleClick: (NewsArticle) -> Unit,
    onAiClick: (NewsArticle) -> Unit,
    onSaveClick: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRegion by remember { mutableStateOf("All India") }

    val categories = listOf(
        CategoryGridItem("National", Icons.Default.Public),
        CategoryGridItem("Politics", Icons.Default.Gavel),
        CategoryGridItem("Business", Icons.Default.Business),
        CategoryGridItem("Sports", Icons.Default.SportsCricket),
        CategoryGridItem("Technology", Icons.Default.Computer),
        CategoryGridItem("Entertainment", Icons.Default.Movie),
        CategoryGridItem("Health", Icons.Default.HealthAndSafety),
        CategoryGridItem("Education", Icons.Default.School),
        CategoryGridItem("Science", Icons.Default.Science),
        CategoryGridItem("World", Icons.Default.CompassCalibration)
    )

    val indianStates = listOf(
        "All India", "Delhi NCR", "Maharashtra", "Karnataka", "Uttar Pradesh",
        "Tamil Nadu", "Gujarat", "West Bengal", "Kerala", "Punjab", "Rajasthan", "Bihar", "Telangana"
    )

    val trendingTopics = listOf(
        "#ISRO #SpaceMission", "#IndianEconomy2026", "#StartupsIndia",
        "#CricketWorldCup", "#G20Summit", "#DigitalIndia", "#GreenEnergy"
    )

    val savedIds = savedArticles.map { it.id }.toSet()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .testTag("discover_screen")
    ) {
        // Search Header Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Discover & Search",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
                Text(
                    text = "Explore real stories by category, region, or trending topics across India",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Search topics, locations, or agencies...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = DeepNavy
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
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
                        .testTag("discover_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }
        }

        // Indian States / Regions Horizontal Selector
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Region",
                        tint = SaffronPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Indian States & Regions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(indianStates) { state ->
                        val isSelected = selectedRegion == state
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedRegion = state
                                if (state != "All India") {
                                    onSearchQueryChanged(state)
                                } else {
                                    onSearchQueryChanged("")
                                }
                            },
                            label = { Text(state, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepNavy,
                                selectedLabelColor = PureWhite,
                                containerColor = SoftGrayBg,
                                labelColor = TextMuted
                            ),
                            border = null,
                            modifier = Modifier.testTag("region_chip_$state")
                        )
                    }
                }
            }
        }

        // Categories Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { item ->
                        Card(
                            modifier = Modifier
                                .clickable { onCategorySelected(item.name) }
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                .testTag("category_grid_${item.name}"),
                            colors = CardDefaults.cardColors(containerColor = SoftGrayBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.name,
                                    tint = DeepNavy,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepNavy
                                )
                            }
                        }
                    }
                }
            }
        }

        // Trending Topics Tags
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = "Trending",
                        tint = SaffronPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Trending Topics in Bharat",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trendingTopics) { topic ->
                        Surface(
                            color = SoftGrayBg,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    val cleaned = topic.replace("#", "").split(" ").firstOrNull() ?: ""
                                    onSearchQueryChanged(cleaned)
                                }
                        ) {
                            Text(
                                text = topic,
                                color = SaffronPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Search Results / Live News Header
        item {
            Text(
                text = if (searchQuery.isNotBlank()) "Search Results for '$searchQuery'" else "Discover Feed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepNavy,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (isLoading) {
            items(4) {
                SkeletonNewsCard()
            }
        } else if (newsArticles.isEmpty()) {
            item {
                EmptyStateView(
                    type = EmptyStateType.NO_SEARCH_RESULTS,
                    title = "No stories found for '$searchQuery'",
                    message = "Try adjusting your search criteria or check Indian top headlines.",
                    onActionClick = { onSearchQueryChanged("") },
                    actionButtonText = "Clear Search"
                )
            }
        } else {
            items(newsArticles) { article ->
                ArticleItemCard(
                    article = article,
                    isSaved = savedIds.contains(article.id),
                    onArticleClick = onArticleClick,
                    onAiClick = onAiClick,
                    onSaveClick = onSaveClick
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
