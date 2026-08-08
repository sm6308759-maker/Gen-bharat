package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsArticle
import com.example.ui.theme.AiPurple
import com.example.ui.theme.AiPurpleBg
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSummaryBottomSheet(
    article: NewsArticle,
    sheetState: SheetState,
    aiSummaryText: String?,
    isGeneratingSummary: Boolean,
    selectedLanguage: String,
    translatedContent: String?,
    isTranslating: Boolean,
    chatMessages: List<Pair<String, String>>,
    isAskingAi: Boolean,
    onLanguageSelected: (String) -> Unit,
    onAskQuestion: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var questionInput by remember { mutableStateOf("") }
    val languages = listOf("English", "Hindi", "Tamil", "Telugu", "Marathi", "Bengali")

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = PureWhite,
        modifier = modifier.testTag("ai_summary_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = AiPurpleBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "GenZ Bharat AI Studio",
                            tint = AiPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GenZ Bharat AI Studio",
                            color = AiPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Sheet",
                        tint = DeepNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Article Title Reference
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: AI Key Takeaways / Summary
            Surface(
                color = SoftGrayBg,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡ AI Executive Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isGeneratingSummary) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = AiPurple
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Gemini AI is analyzing article key takeaways...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    } else {
                        Text(
                            text = aiSummaryText ?: "No AI summary available.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = DeepNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Regional Language Translator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language Translation",
                    tint = DeepNavy,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Indian Regional Translation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(languages) { lang ->
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = { onLanguageSelected(lang) },
                        label = { Text(lang, fontSize = 12.sp, fontWeight = if (selectedLanguage == lang) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepNavy,
                            selectedLabelColor = PureWhite,
                            containerColor = SoftGrayBg,
                            labelColor = TextMuted
                        ),
                        border = null
                    )
                }
            }

            AnimatedVisibility(visible = selectedLanguage != "English") {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    if (isTranslating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = DeepNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Translating to $selectedLanguage...", fontSize = 12.sp, color = TextMuted)
                        }
                    } else if (translatedContent != null) {
                        Surface(
                            color = SoftGrayBg,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = translatedContent,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                color = DeepNavy
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = CardBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Ask AI About This News (Interactive Chat)
            Text(
                text = "💬 Ask GenZ Bharat AI",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Q&A History
            chatMessages.forEach { (q, a) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Question Bubble
                    Surface(
                        color = DeepNavy,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = q,
                            modifier = Modifier.padding(10.dp),
                            fontSize = 13.sp,
                            color = PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Answer Bubble
                    Surface(
                        color = SoftGrayBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = a,
                            modifier = Modifier.padding(10.dp),
                            fontSize = 13.sp,
                            color = DeepNavy
                        )
                    }
                }
            }

            if (isAskingAi) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = DeepNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI is processing your question...", fontSize = 12.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = questionInput,
                    onValueChange = { questionInput = it },
                    placeholder = { Text("e.g., What does this mean for consumers?", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ask_ai_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (questionInput.isNotBlank()) {
                            onAskQuestion(questionInput)
                            questionInput = ""
                        }
                    },
                    modifier = Modifier.testTag("ask_ai_send_button")
                ) {
                    Surface(
                        color = DeepNavy,
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Question",
                            tint = PureWhite,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}
