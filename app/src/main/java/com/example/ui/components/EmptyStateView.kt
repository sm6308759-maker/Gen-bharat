package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.TextMuted

enum class EmptyStateType {
    NO_NEWS,
    NO_SEARCH_RESULTS,
    NO_BOOKMARKS,
    API_ERROR
}

@Composable
fun EmptyStateView(
    type: EmptyStateType,
    title: String? = null,
    message: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionButtonText: String? = null,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector
    val defaultTitle: String
    val defaultMessage: String

    when (type) {
        EmptyStateType.NO_NEWS -> {
            icon = Icons.Default.Newspaper
            defaultTitle = "No Stories Available"
            defaultMessage = "Check back shortly or refresh to pull the latest news feeds from India."
        }
        EmptyStateType.NO_SEARCH_RESULTS -> {
            icon = Icons.Default.SearchOff
            defaultTitle = "No Results Found"
            defaultMessage = "Try searching with different keywords or selecting a category."
        }
        EmptyStateType.NO_BOOKMARKS -> {
            icon = Icons.Default.BookmarkBorder
            defaultTitle = "No Saved Articles"
            defaultMessage = "Articles you bookmark while reading will appear here for easy offline access."
        }
        EmptyStateType.API_ERROR -> {
            icon = Icons.Default.ErrorOutline
            defaultTitle = "Connection Issue"
            defaultMessage = "Unable to reach news server. Please verify your internet connection."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(SoftGrayBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Empty Icon",
                tint = DeepNavy,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title ?: defaultTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepNavy,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message ?: defaultMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        if (onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepNavy,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(
                    text = actionButtonText ?: "Refresh",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
