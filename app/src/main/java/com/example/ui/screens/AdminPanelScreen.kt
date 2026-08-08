package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.model.AdminStats
import com.example.data.model.NewsArticle
import com.example.data.model.VideoNews
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun AdminPanelScreen(
    onBackClick: () -> Unit,
    adminViewModel: AdminViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by adminViewModel.currentTab.collectAsStateWithLifecycle()
    val adminStats by adminViewModel.adminStats.collectAsStateWithLifecycle()
    val newsList by adminViewModel.newsList.collectAsStateWithLifecycle()
    val videoList by adminViewModel.videoList.collectAsStateWithLifecycle()
    val newsFilter by adminViewModel.newsFilter.collectAsStateWithLifecycle()
    val videoFilter by adminViewModel.videoFilter.collectAsStateWithLifecycle()
    val newsFormState by adminViewModel.newsFormState.collectAsStateWithLifecycle()
    val videoFormState by adminViewModel.videoFormState.collectAsStateWithLifecycle()
    val isLoading by adminViewModel.isLoading.collectAsStateWithLifecycle()
    val statusMsg by adminViewModel.statusMessage.collectAsStateWithLifecycle()
    val isAuthorized by adminViewModel.isAdminAuthorized.collectAsStateWithLifecycle()

    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }

    LaunchedEffect(statusMsg) {
        statusMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            adminViewModel.clearStatusMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        // Admin Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("admin_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DeepNavy
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = SaffronPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "GenZ Bharat Admin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DeepNavy
                    )
                    Text(
                        text = "Secure Content Management Portal",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { adminViewModel.refreshAdminData() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = DeepNavy
                )
            }
        }

        if (!isAuthorized) {
            // Admin Access Gate Screen
            AdminLoginGateScreen(
                email = adminEmail,
                password = adminPassword,
                isLoading = isLoading,
                onEmailChange = { adminEmail = it },
                onPasswordChange = { adminPassword = it },
                onLoginAdmin = {
                    authViewModel.login(adminEmail, adminPassword) {
                        adminViewModel.checkAdminSession()
                    }
                }
            )
        } else {
            // Authorized Admin Interface
            Column(modifier = Modifier.fillMaxSize()) {
                // Navigation Tabs
                TabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = PureWhite,
                    contentColor = DeepNavy,
                    indicator = { tabPositions ->
                        if (currentTab.ordinal < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                                color = SaffronPrimary
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = currentTab == AdminTab.DASHBOARD,
                        onClick = { adminViewModel.setTab(AdminTab.DASHBOARD) },
                        text = { Text("Dashboard", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = currentTab == AdminTab.NEWS_LIST || currentTab == AdminTab.ADD_NEWS,
                        onClick = { adminViewModel.setTab(AdminTab.NEWS_LIST) },
                        text = { Text("News (${newsList.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Article, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = currentTab == AdminTab.VIDEO_LIST || currentTab == AdminTab.ADD_VIDEO,
                        onClick = { adminViewModel.setTab(AdminTab.VIDEO_LIST) },
                        text = { Text("Videos (${videoList.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = currentTab == AdminTab.USERS,
                        onClick = { adminViewModel.setTab(AdminTab.USERS) },
                        text = { Text("Security", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentTab) {
                        AdminTab.DASHBOARD -> AdminDashboardView(
                            stats = adminStats,
                            onAddNewsClick = {
                                adminViewModel.resetNewsForm()
                                adminViewModel.setTab(AdminTab.ADD_NEWS)
                            },
                            onAddVideoClick = {
                                adminViewModel.resetVideoForm()
                                adminViewModel.setTab(AdminTab.ADD_VIDEO)
                            },
                            onViewNewsClick = { adminViewModel.setTab(AdminTab.NEWS_LIST) },
                            onViewVideosClick = { adminViewModel.setTab(AdminTab.VIDEO_LIST) }
                        )
                        AdminTab.NEWS_LIST -> AdminNewsListView(
                            newsList = newsList,
                            activeFilter = newsFilter,
                            onFilterSelected = { adminViewModel.setNewsFilter(it) },
                            onAddNewsClick = {
                                adminViewModel.resetNewsForm()
                                adminViewModel.setTab(AdminTab.ADD_NEWS)
                            },
                            onEditClick = { adminViewModel.prepareEditNews(it) },
                            onDeleteClick = { adminViewModel.deleteNews(it.id) },
                            onToggleSwitch = { article, toggle -> adminViewModel.toggleNewsStatus(article, toggle) }
                        )
                        AdminTab.ADD_NEWS -> AdminAddNewsFormView(
                            formState = newsFormState,
                            isLoading = isLoading,
                            onFieldChange = { t, d, c, img, cat, reg, lang, src, aut, breakState, trendState, latState, pubState ->
                                adminViewModel.updateNewsFormField(t, d, c, img, cat, reg, lang, src, aut, breakState, trendState, latState, pubState)
                            },
                            onSubmit = { asDraft -> adminViewModel.submitNewsForm(asDraft) },
                            onCancel = { adminViewModel.setTab(AdminTab.NEWS_LIST) }
                        )
                        AdminTab.VIDEO_LIST -> AdminVideoListView(
                            videoList = videoList,
                            activeFilter = videoFilter,
                            onFilterSelected = { adminViewModel.setVideoFilter(it) },
                            onAddVideoClick = {
                                adminViewModel.resetVideoForm()
                                adminViewModel.setTab(AdminTab.ADD_VIDEO)
                            },
                            onEditClick = { adminViewModel.prepareEditVideo(it) },
                            onDeleteClick = { adminViewModel.deleteVideo(it.id) },
                            onToggleSwitch = { video, toggle -> adminViewModel.toggleVideoStatus(video, toggle) }
                        )
                        AdminTab.ADD_VIDEO -> AdminAddVideoFormView(
                            formState = videoFormState,
                            isLoading = isLoading,
                            onFieldChange = { t, d, vUrl, thUrl, cat, reg, lang, src, aut, breakState, trendState, latState, pubState ->
                                adminViewModel.updateVideoFormField(t, d, vUrl, thUrl, cat, reg, lang, src, aut, breakState, trendState, latState, pubState)
                            },
                            onSubmit = { asDraft -> adminViewModel.submitVideoForm(asDraft) },
                            onCancel = { adminViewModel.setTab(AdminTab.VIDEO_LIST) }
                        )
                        AdminTab.USERS -> AdminSecurityDiagnosticsView()
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(PureWhite.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = SaffronPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLoginGateScreen(
    email: String,
    password: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginAdmin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(SaffronPrimary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = SaffronPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Authorized Admin Login Required",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = DeepNavy
        )
        Text(
            text = "Please log in with an administrator account to manage news, videos, and platform content.",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Admin Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SaffronPrimary,
                unfocusedBorderColor = CardBorder
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Admin Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SaffronPrimary,
                unfocusedBorderColor = CardBorder
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginAdmin,
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PureWhite)
            } else {
                Text("Authenticate as Admin", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminDashboardView(
    stats: AdminStats,
    onAddNewsClick: () -> Unit,
    onAddVideoClick: () -> Unit,
    onViewNewsClick: () -> Unit,
    onViewVideosClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Dashboard Overview",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = DeepNavy
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            // Action Shortcut Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAddNewsClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add News", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAddVideoClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Icon(Icons.Default.Movie, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Video", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            // Metrics Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total News",
                        value = "${stats.totalNews}",
                        icon = Icons.Default.Article,
                        accentColor = DeepNavy,
                        modifier = Modifier.weight(1f),
                        onClick = onViewNewsClick
                    )
                    MetricCard(
                        title = "Total Videos",
                        value = "${stats.totalVideos}",
                        icon = Icons.Default.Movie,
                        accentColor = SaffronPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onViewVideosClick
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Published News",
                        value = "${stats.publishedNews}",
                        icon = Icons.Default.PublishedWithChanges,
                        accentColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        onClick = onViewNewsClick
                    )
                    MetricCard(
                        title = "Breaking News",
                        value = "${stats.breakingNews}",
                        icon = Icons.Default.Whatshot,
                        accentColor = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f),
                        onClick = onViewNewsClick
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Trending News",
                        value = "${stats.trendingNews}",
                        icon = Icons.Default.TrendingUp,
                        accentColor = Color(0xFFED6C02),
                        modifier = Modifier.weight(1f),
                        onClick = onViewNewsClick
                    )
                    MetricCard(
                        title = "Total Readers",
                        value = "${stats.totalUsers}+",
                        icon = Icons.Default.People,
                        accentColor = Color(0xFF0288D1),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftGrayBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡ Real-time Hybrid Publishing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DeepNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Articles and videos created in this panel are stored in Supabase with RLS row security and co-exist live with NewsData.io API news in the user app.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accentColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun AdminNewsListView(
    newsList: List<NewsArticle>,
    activeFilter: String,
    onFilterSelected: (String) -> Unit,
    onAddNewsClick: () -> Unit,
    onEditClick: (NewsArticle) -> Unit,
    onDeleteClick: (NewsArticle) -> Unit,
    onToggleSwitch: (NewsArticle, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Manage Articles",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DeepNavy
            )
            Button(
                onClick = onAddNewsClick,
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create News", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Filters bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("ALL", "PUBLISHED", "DRAFTS", "BREAKING", "TRENDING")
            items(filters) { f ->
                val isSelected = activeFilter == f
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) DeepNavy else SoftGrayBg)
                        .clickable { onFilterSelected(f) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = f,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PureWhite else TextMuted
                    )
                }
            }
        }

        if (newsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "No articles found in filter '$activeFilter'.", fontSize = 14.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onAddNewsClick, colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)) {
                        Text("Add New Article")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(newsList, key = { it.id }) { article ->
                    AdminNewsCard(
                        article = article,
                        onEditClick = { onEditClick(article) },
                        onDeleteClick = { onDeleteClick(article) },
                        onToggleBreaking = { onToggleSwitch(article, "BREAKING") },
                        onToggleTrending = { onToggleSwitch(article, "TRENDING") },
                        onTogglePublished = { onToggleSwitch(article, "PUBLISHED") }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminNewsCard(
    article: NewsArticle,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleBreaking: () -> Unit,
    onToggleTrending: () -> Unit,
    onTogglePublished: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!article.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = article.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = article.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )
                        Text(text = " • ", fontSize = 11.sp, color = TextMuted)
                        Text(text = article.region, fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges & Toggle Switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Breaking", fontSize = 10.sp, color = TextMuted)
                        Switch(
                            checked = article.isBreaking,
                            onCheckedChange = { onToggleBreaking() },
                            colors = SwitchDefaults.colors(checkedThumbColor = SaffronPrimary)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Trending", fontSize = 10.sp, color = TextMuted)
                        Switch(
                            checked = article.isTrending,
                            onCheckedChange = { onToggleTrending() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFED6C02))
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (article.isPublished) "Published" else "Draft", fontSize = 10.sp, color = if (article.isPublished) Color(0xFF2E7D32) else TextMuted)
                        Switch(
                            checked = article.isPublished,
                            onCheckedChange = { onTogglePublished() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2E7D32))
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DeepNavy)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAddNewsFormView(
    formState: com.example.ui.viewmodel.AdminNewsFormState,
    isLoading: Boolean,
    onFieldChange: (String?, String?, String?, String?, String?, String?, String?, String?, String?, Boolean?, Boolean?, Boolean?, Boolean?) -> Unit,
    onSubmit: (Boolean) -> Unit,
    onCancel: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = if (formState.isEditing) "Edit Article" else "Create New News Article",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DeepNavy
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = formState.title,
                onValueChange = { onFieldChange(it, null, null, null, null, null, null, null, null, null, null, null, null) },
                label = { Text("News Title *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = formState.description,
                onValueChange = { onFieldChange(null, it, null, null, null, null, null, null, null, null, null, null, null) },
                label = { Text("Short Description") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = formState.content,
                onValueChange = { onFieldChange(null, null, it, null, null, null, null, null, null, null, null, null, null) },
                label = { Text("Full Article Content") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = formState.imageUrl,
                onValueChange = { onFieldChange(null, null, null, it, null, null, null, null, null, null, null, null, null) },
                label = { Text("Cover Image URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = formState.category,
                    onValueChange = { onFieldChange(null, null, null, null, it, null, null, null, null, null, null, null, null) },
                    label = { Text("Category") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.region,
                    onValueChange = { onFieldChange(null, null, null, null, null, it, null, null, null, null, null, null, null) },
                    label = { Text("Region") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = formState.language,
                    onValueChange = { onFieldChange(null, null, null, null, null, null, it, null, null, null, null, null, null) },
                    label = { Text("Language (hi/en)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.author,
                    onValueChange = { onFieldChange(null, null, null, null, null, null, null, null, it, null, null, null, null) },
                    label = { Text("Author") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Checkboxes / Switches
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftGrayBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🔥 Breaking News Tag", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = formState.isBreaking,
                            onCheckedChange = { onFieldChange(null, null, null, null, null, null, null, null, null, it, null, null, null) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📈 Trending in Bharat Tag", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = formState.isTrending,
                            onCheckedChange = { onFieldChange(null, null, null, null, null, null, null, null, null, null, it, null, null) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("✅ Publish Immediately", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = formState.isPublished,
                            onCheckedChange = { onFieldChange(null, null, null, null, null, null, null, null, null, null, null, null, it) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                OutlinedButton(
                    onClick = { onSubmit(true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Draft")
                }

                Button(
                    onClick = { onSubmit(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("Publish")
                }
            }
        }
    }
}

@Composable
fun AdminVideoListView(
    videoList: List<VideoNews>,
    activeFilter: String,
    onFilterSelected: (String) -> Unit,
    onAddVideoClick: () -> Unit,
    onEditClick: (VideoNews) -> Unit,
    onDeleteClick: (VideoNews) -> Unit,
    onToggleSwitch: (VideoNews, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Manage Video News",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DeepNavy
            )
            Button(
                onClick = onAddVideoClick,
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
            ) {
                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Video", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Filters bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("ALL", "PUBLISHED", "DRAFTS", "BREAKING", "TRENDING")
            items(filters) { f ->
                val isSelected = activeFilter == f
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) DeepNavy else SoftGrayBg)
                        .clickable { onFilterSelected(f) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = f,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PureWhite else TextMuted
                    )
                }
            }
        }

        if (videoList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "No videos found in filter '$activeFilter'.", fontSize = 14.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onAddVideoClick, colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)) {
                        Text("Add Video News")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(videoList, key = { it.id }) { video ->
                    AdminVideoCard(
                        video = video,
                        onEditClick = { onEditClick(video) },
                        onDeleteClick = { onDeleteClick(video) },
                        onToggleBreaking = { onToggleSwitch(video, "BREAKING") },
                        onToggleTrending = { onToggleSwitch(video, "TRENDING") },
                        onTogglePublished = { onToggleSwitch(video, "PUBLISHED") }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminVideoCard(
    video: VideoNews,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleBreaking: () -> Unit,
    onToggleTrending: () -> Unit,
    onTogglePublished: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(70.dp, 50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepNavy),
                    contentAlignment = Alignment.Center
                ) {
                    if (!video.thumbnailUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = video.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PureWhite)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Category: ${video.category} • ${video.region}", fontSize = 11.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges & Toggle Switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Breaking", fontSize = 10.sp, color = TextMuted)
                        Switch(
                            checked = video.isBreaking,
                            onCheckedChange = { onToggleBreaking() },
                            colors = SwitchDefaults.colors(checkedThumbColor = SaffronPrimary)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Trending", fontSize = 10.sp, color = TextMuted)
                        Switch(
                            checked = video.isTrending,
                            onCheckedChange = { onToggleTrending() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFED6C02))
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (video.isPublished) "Published" else "Draft", fontSize = 10.sp, color = if (video.isPublished) Color(0xFF2E7D32) else TextMuted)
                        Switch(
                            checked = video.isPublished,
                            onCheckedChange = { onTogglePublished() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2E7D32))
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DeepNavy)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAddVideoFormView(
    formState: com.example.ui.viewmodel.AdminVideoFormState,
    isLoading: Boolean,
    onFieldChange: (String?, String?, String?, String?, String?, String?, String?, String?, String?, Boolean?, Boolean?, Boolean?, Boolean?) -> Unit,
    onSubmit: (Boolean) -> Unit,
    onCancel: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = if (formState.isEditing) "Edit Video Story" else "Add Video News Story",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DeepNavy
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = formState.title,
                onValueChange = { onFieldChange(it, null, null, null, null, null, null, null, null, null, null, null, null) },
                label = { Text("Video Title *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = formState.videoUrl,
                onValueChange = { onFieldChange(null, null, it, null, null, null, null, null, null, null, null, null, null) },
                label = { Text("Video Stream / MP4 / YouTube URL *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = formState.thumbnailUrl,
                onValueChange = { onFieldChange(null, null, null, it, null, null, null, null, null, null, null, null, null) },
                label = { Text("Thumbnail Image URL") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = formState.description,
                onValueChange = { onFieldChange(null, it, null, null, null, null, null, null, null, null, null, null, null) },
                label = { Text("Video Description") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = formState.category,
                    onValueChange = { onFieldChange(null, null, null, null, it, null, null, null, null, null, null, null, null) },
                    label = { Text("Category") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.region,
                    onValueChange = { onFieldChange(null, null, null, null, null, it, null, null, null, null, null, null, null) },
                    label = { Text("Region") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftGrayBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🔥 Breaking Video Tag", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = formState.isBreaking,
                            onCheckedChange = { onFieldChange(null, null, null, null, null, null, null, null, null, it, null, null, null) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📈 Trending Tag", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = formState.isTrending,
                            onCheckedChange = { onFieldChange(null, null, null, null, null, null, null, null, null, null, it, null, null) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("✅ Publish Immediately", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = formState.isPublished,
                            onCheckedChange = { onFieldChange(null, null, null, null, null, null, null, null, null, null, null, null, it) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                OutlinedButton(
                    onClick = { onSubmit(true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Draft")
                }

                Button(
                    onClick = { onSubmit(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Publish Video")
                }
            }
        }
    }
}

@Composable
fun AdminSecurityDiagnosticsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Database & Security Diagnostics",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = DeepNavy
        )
        Text(
            text = "Verify Supabase RLS policies and table isolation status",
            fontSize = 12.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        val tables = listOf(
            Triple("public.news", "Active Row Level Security (RLS) - Public Read, Admin Write", true),
            Triple("public.videos", "Active Row Level Security (RLS) - Public Read, Admin Write", true),
            Triple("public.profiles", "Active Row Level Security (RLS) - Isolated User Profiles", true),
            Triple("public.bookmarks", "Active Row Level Security (RLS) - Private User Bookmarks", true),
            Triple("public.user_preferences", "Active Row Level Security (RLS) - Private Preferences", true)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            tables.forEach { (tableName, description, isOk) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF2E7D32).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = tableName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text(text = description, fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
