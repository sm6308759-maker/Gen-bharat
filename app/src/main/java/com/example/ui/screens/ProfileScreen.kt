package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import com.example.data.model.UserSession
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.TextMuted

@Composable
fun ProfileScreen(
    session: UserSession,
    newsLanguage: String,
    notificationsEnabled: Boolean,
    onLanguageSelected: (String) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAdmin: (() -> Unit)? = null,
    onUpdateProfileName: ((String) -> Unit)? = null,
    onDeleteAccount: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("profile_screen")
    ) {
        // User Profile Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SoftGrayBg)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(DeepNavy, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = PureWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (session.isLoggedIn && session.user != null) session.user.fullName else "Guest User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            if (session.isLoggedIn) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Verified Member",
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (session.isLoggedIn && session.user != null) session.user.email else "Browsing as guest",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            color = if (session.isLoggedIn) EmeraldGreen.copy(alpha = 0.15f) else SaffronPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (session.isLoggedIn && session.user != null)
                                    "Member since ${session.user.createdAt} • Supabase Auth Active"
                                else
                                    "Guest Mode • Local Bookmarks Active",
                                color = if (session.isLoggedIn) EmeraldGreen else DeepNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (session.isLoggedIn) {
                    OutlinedButton(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("logout_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD32F2F)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFFD32F2F))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Sign Out from Supabase", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = onNavigateToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("profile_signin_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite)
                    ) {
                        Text(text = "Sign In / Register Account", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bookmark Sync Status Row
        ProfileSettingRow(
            icon = if (session.isLoggedIn) Icons.Default.CloudDone else Icons.Default.CloudOff,
            title = "Cloud Bookmark Synchronization",
            subtitle = if (session.isLoggedIn)
                "Local Room bookmarks automatically sync to Supabase Cloud"
            else
                "Sign in to enable cross-device cloud bookmark sync",
            onClick = {
                if (session.isLoggedIn) {
                    Toast.makeText(context, "Bookmarks synced with Supabase Cloud!", Toast.LENGTH_SHORT).show()
                } else {
                    onNavigateToLogin()
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // App Preferences Header
        Text(
            text = "App Preferences",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = DeepNavy
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Language Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = DeepNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "News Language Preference",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepNavy,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { onLanguageSelected("en") },
                        color = if (newsLanguage == "en") DeepNavy else SoftGrayBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "English Feed",
                                color = if (newsLanguage == "en") PureWhite else DeepNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Surface(
                        onClick = { onLanguageSelected("hi") },
                        color = if (newsLanguage == "hi") DeepNavy else SoftGrayBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "हिंदी समाचार (Hindi)",
                                color = if (newsLanguage == "hi") PureWhite else DeepNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Notifications Toggle Row
        ProfileSettingRow(
            icon = Icons.Default.Notifications,
            title = "Breaking News Notifications",
            trailing = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsToggled,
                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = SaffronPrimary)
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Cache Management Row
        ProfileSettingRow(
            icon = Icons.Default.Cached,
            title = "Offline Cache Storage",
            subtitle = "Articles stored locally in Room DB for offline reading",
            onClick = {
                Toast.makeText(context, "Local Room database active for offline reading", Toast.LENGTH_SHORT).show()
            }
        )

        if (session.isLoggedIn && session.user != null) {
            Spacer(modifier = Modifier.height(20.dp))

            // Account Management Section
            Text(
                text = "Account Management",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProfileSettingRow(
                icon = Icons.Default.Edit,
                title = "Edit Profile Name",
                subtitle = "Update full name on Supabase Cloud",
                onClick = { showEditProfileDialog = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProfileSettingRow(
                icon = Icons.Default.Delete,
                title = "Delete Account",
                subtitle = "Permanently remove profile and cloud bookmarks",
                onClick = { showDeleteAccountDialog = true }
            )
        }

        val isAdmin = session.isLoggedIn && (session.user?.role?.equals("admin", ignoreCase = true) == true)

        if (isAdmin) {
            Spacer(modifier = Modifier.height(20.dp))

            // Administration
            Text(
                text = "Administration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProfileSettingRow(
                icon = Icons.Default.AdminPanelSettings,
                title = "Admin Panel",
                subtitle = "Manage news articles, video stories, breaking news & stats",
                onClick = { onNavigateToAdmin?.invoke() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // About & Legal
        Text(
            text = "About & Legal",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = DeepNavy
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProfileSettingRow(
            icon = Icons.Default.Shield,
            title = "Privacy Policy",
            onClick = {
                Toast.makeText(context, "Privacy Policy: User data is kept secure.", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProfileSettingRow(
            icon = Icons.Default.PrivacyTip,
            title = "Terms of Service",
            onClick = {
                Toast.makeText(context, "Terms of Service: Powered by NewsData.io & Gemini AI.", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProfileSettingRow(
            icon = Icons.Default.Info,
            title = "About GenZ Bharat",
            subtitle = "Version 2.0.0-IN • NewsData.io & Supabase Auth",
            onClick = {
                Toast.makeText(context, "GenZ Bharat v2.0.0 - Supabase Enabled", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = session.user?.fullName ?: "",
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName ->
                onUpdateProfileName?.invoke(newName)
                showEditProfileDialog = false
            }
        )
    }

    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onConfirm = {
                showDeleteAccountDialog = false
                onDeleteAccount?.invoke()
            }
        )
    }
}

@Composable
fun ProfileSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SoftGrayBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = DeepNavy,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepNavy
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Arrow",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Profile", fontWeight = FontWeight.Bold, color = DeepNavy)
        },
        text = {
            Column {
                Text(
                    text = "Update your full name saved in Supabase Cloud profile.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite)
            ) {
                Text("Update Name")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = PureWhite
    )
}

@Composable
private fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Delete Account?", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFFD32F2F))
        },
        text = {
            Text(
                text = "Are you sure you want to permanently delete your profile and cloud bookmarks? This action cannot be undone.",
                fontSize = 13.sp,
                color = DeepNavy
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFD32F2F), contentColor = PureWhite)
            ) {
                Text("Delete Permanently")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = PureWhite
    )
}
