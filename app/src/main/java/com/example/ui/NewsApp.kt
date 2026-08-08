package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.NewsArticle
import com.example.ui.components.AiSummaryBottomSheet
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.ArticleDetailScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SignupScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.NewsViewModel

enum class NavTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    DISCOVER("Discover", Icons.Filled.Explore, Icons.Outlined.Explore),
    BOOKMARKS("Bookmarks", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    AI_STUDIO("AI Studio", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

enum class AuthRoute {
    WELCOME,
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    NONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsApp(
    viewModel: NewsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session by authViewModel.sessionState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    val newsList by viewModel.newsList.collectAsStateWithLifecycle()
    val breakingList by viewModel.breakingNewsList.collectAsStateWithLifecycle()
    val videoList by viewModel.videoList.collectAsStateWithLifecycle()
    val savedList by viewModel.savedArticles.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val newsLanguage by viewModel.newsLanguage.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val selectedArticleForAi by viewModel.selectedArticleForAi.collectAsStateWithLifecycle()
    val aiSummaryText by viewModel.aiSummaryText.collectAsStateWithLifecycle()
    val isGeneratingSummary by viewModel.isGeneratingAiSummary.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedTranslationLang.collectAsStateWithLifecycle()
    val translatedContent by viewModel.translatedContent.collectAsStateWithLifecycle()
    val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()
    val chatMessages by viewModel.aiChatMessages.collectAsStateWithLifecycle()
    val isAskingAi by viewModel.isAskingAi.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var activeDetailArticle by remember { mutableStateOf<NewsArticle?>(null) }
    var showAdminPanel by remember { mutableStateOf(false) }
    var authRoute by remember { mutableStateOf(AuthRoute.NONE) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val savedIds = savedList.map { it.id }.toSet()

    val showAuthFlow = !session.isLoggedIn && !session.isGuest

    if (showAuthFlow || authRoute != AuthRoute.NONE) {
        val currentAuthRoute = if (showAuthFlow && authRoute == AuthRoute.NONE) AuthRoute.WELCOME else authRoute

        when (currentAuthRoute) {
            AuthRoute.WELCOME -> WelcomeScreen(
                onNavigateToLogin = {
                    authViewModel.clearMessages()
                    authRoute = AuthRoute.LOGIN
                },
                onNavigateToSignup = {
                    authViewModel.clearMessages()
                    authRoute = AuthRoute.SIGNUP
                },
                onContinueAsGuest = {
                    authViewModel.continueAsGuest()
                    authRoute = AuthRoute.NONE
                }
            )
            AuthRoute.LOGIN -> LoginScreen(
                isLoading = authUiState.isLoading,
                errorMessage = authUiState.errorMessage,
                onLoginClick = { email, password ->
                    authViewModel.login(email, password) {
                        authRoute = AuthRoute.NONE
                    }
                },
                onForgotPasswordClick = {
                    authViewModel.clearMessages()
                    authRoute = AuthRoute.FORGOT_PASSWORD
                },
                onNavigateToSignup = {
                    authViewModel.clearMessages()
                    authRoute = AuthRoute.SIGNUP
                },
                onBackClick = { authRoute = AuthRoute.WELCOME }
            )
            AuthRoute.SIGNUP -> SignupScreen(
                isLoading = authUiState.isLoading,
                errorMessage = authUiState.errorMessage,
                onSignupClick = { name, email, password, confirmPass ->
                    authViewModel.signUp(name, email, password, confirmPass) {
                        authRoute = AuthRoute.NONE
                    }
                },
                onNavigateToLogin = {
                    authViewModel.clearMessages()
                    authRoute = AuthRoute.LOGIN
                },
                onBackClick = { authRoute = AuthRoute.WELCOME }
            )
            AuthRoute.FORGOT_PASSWORD -> ForgotPasswordScreen(
                isLoading = authUiState.isLoading,
                errorMessage = authUiState.errorMessage,
                successMessage = authUiState.successMessage,
                isResetSent = authUiState.isResetSent,
                onResetPasswordClick = { email ->
                    authViewModel.forgotPassword(email)
                },
                onBackToLoginClick = {
                    authViewModel.clearMessages()
                    authRoute = AuthRoute.LOGIN
                },
                onBackClick = { authRoute = AuthRoute.LOGIN }
            )
            AuthRoute.NONE -> {
                authRoute = AuthRoute.NONE
            }
        }
        return
    }

    Scaffold(
        topBar = {
            if (activeDetailArticle == null && !showAdminPanel) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GENZ BHARAT",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp,
                                color = DeepNavy
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(SaffronPrimary, CircleShape)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { currentTab = NavTab.DISCOVER }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search News",
                                tint = DeepNavy
                            )
                        }
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "No new breaking notifications", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = DeepNavy
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
                )
            }
        },
        bottomBar = {
            if (activeDetailArticle == null && !showAdminPanel) {
                NavigationBar(
                    containerColor = PureWhite,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .border(width = 1.dp, color = CardBorder, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .testTag("bottom_navigation_bar")
                ) {
                    NavTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DeepNavy,
                                selectedTextColor = DeepNavy,
                                indicatorColor = SoftGrayBg,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        containerColor = PureWhite,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (showAdminPanel) {
            AdminPanelScreen(
                onBackClick = {
                    showAdminPanel = false
                    viewModel.loadNewsFeed()
                },
                modifier = Modifier.padding(innerPadding)
            )
        } else if (activeDetailArticle != null) {
            val article = activeDetailArticle!!
            ArticleDetailScreen(
                article = article,
                isSaved = savedIds.contains(article.id),
                onBackClick = { activeDetailArticle = null },
                onAiClick = { viewModel.openAiSheetForArticle(it) },
                onSaveClick = { viewModel.toggleSaveArticle(it) }
            )
        } else {
            when (currentTab) {
                NavTab.HOME -> HomeScreen(
                    newsArticles = newsList,
                    breakingArticles = breakingList,
                    savedArticles = savedList,
                    videoList = videoList,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    newsLanguage = newsLanguage,
                    isLoading = isLoading,
                    isOfflineMode = isOfflineMode,
                    errorMessage = errorMessage,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onLanguageSelected = { viewModel.setNewsLanguage(it) },
                    onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                    onRetryClick = { viewModel.loadNewsFeed() },
                    onArticleClick = { activeDetailArticle = it },
                    onAiClick = { viewModel.openAiSheetForArticle(it) },
                    onSaveClick = { viewModel.toggleSaveArticle(it) },
                    modifier = Modifier.padding(innerPadding)
                )
                NavTab.DISCOVER -> DiscoverScreen(
                    newsArticles = newsList,
                    savedArticles = savedList,
                    searchQuery = searchQuery,
                    isLoading = isLoading,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                    onArticleClick = { activeDetailArticle = it },
                    onAiClick = { viewModel.openAiSheetForArticle(it) },
                    onSaveClick = { viewModel.toggleSaveArticle(it) },
                    modifier = Modifier.padding(innerPadding)
                )
                NavTab.BOOKMARKS -> BookmarksScreen(
                    savedArticles = savedList,
                    onArticleClick = { activeDetailArticle = it },
                    onAiClick = { viewModel.openAiSheetForArticle(it) },
                    onSaveClick = { viewModel.toggleSaveArticle(it) },
                    modifier = Modifier.padding(innerPadding)
                )
                NavTab.AI_STUDIO -> AiStudioScreen(
                    modifier = Modifier.padding(innerPadding)
                )
                NavTab.PROFILE -> ProfileScreen(
                    session = session,
                    newsLanguage = newsLanguage,
                    notificationsEnabled = notificationsEnabled,
                    onLanguageSelected = { viewModel.setNewsLanguage(it) },
                    onNotificationsToggled = { viewModel.setNotificationsEnabled(it) },
                    onLogoutClick = {
                        authViewModel.logout()
                        authRoute = AuthRoute.WELCOME
                    },
                    onNavigateToLogin = {
                        authViewModel.clearMessages()
                        authRoute = AuthRoute.LOGIN
                    },
                    onNavigateToAdmin = {
                        showAdminPanel = true
                    },
                    onUpdateProfileName = { newName ->
                        authViewModel.updateProfileName(newName)
                    },
                    onDeleteAccount = {
                        authViewModel.deleteAccount {
                            authRoute = AuthRoute.WELCOME
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // AI Modal Bottom Sheet
        if (selectedArticleForAi != null) {
            AiSummaryBottomSheet(
                article = selectedArticleForAi!!,
                sheetState = sheetState,
                aiSummaryText = aiSummaryText,
                isGeneratingSummary = isGeneratingSummary,
                selectedLanguage = selectedLanguage,
                translatedContent = translatedContent,
                isTranslating = isTranslating,
                chatMessages = chatMessages,
                isAskingAi = isAskingAi,
                onLanguageSelected = { viewModel.translateArticle(it) },
                onAskQuestion = { viewModel.askAiQuestion(it) },
                onDismissRequest = { viewModel.closeAiSheet() }
            )
        }
    }
}
