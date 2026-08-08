package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.GeminiApiService
import com.example.ui.theme.AiPurple
import com.example.ui.theme.AiPurpleBg
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun AiStudioScreen(
    modifier: Modifier = Modifier
) {
    var topicInput by remember { mutableStateOf("") }
    var aiResultText by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiApiService() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("ai_studio_screen")
    ) {
        // Hero Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SoftGrayBg)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = DeepNavy,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "GEMINI 3.5 FLASH",
                            color = PureWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "GenZ Bharat AI Studio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Generate cross-source summaries, policy insights, and instant Q&A on Indian national events.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Mind",
                    tint = AiPurple,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Topic Deep Dive Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = AiPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Topic Deep Dive & Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Ask AI to summarize developments on any topic (e.g. 'UPI international adoption', 'ISRO Gaganyaan flight', 'Union Budget priorities').",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        placeholder = { Text("Enter topic or issue...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_studio_topic_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (topicInput.isNotBlank()) {
                                coroutineScope.launch {
                                    isAnalyzing = true
                                    val dummyArticle = com.example.data.model.NewsArticle(
                                        id = "custom_topic",
                                        title = topicInput,
                                        description = "Deep dive query on $topicInput in India context",
                                        content = topicInput
                                    )
                                    val response = geminiService.askAiQuestion(dummyArticle, "Provide a comprehensive synthesis and key takeaways on $topicInput for India.")
                                    aiResultText = response
                                    isAnalyzing = false
                                }
                            }
                        },
                        modifier = Modifier.testTag("ai_studio_analyze_button")
                    ) {
                        Surface(
                            color = DeepNavy,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Run Analysis",
                                tint = PureWhite,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                if (isAnalyzing) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = DeepNavy)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Gemini AI is generating synthesis...", fontSize = 13.sp, color = DeepNavy)
                    }
                } else if (aiResultText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = AiPurpleBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "✨ AI Intelligence Synthesis",
                                fontWeight = FontWeight.Bold,
                                color = AiPurple,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = aiResultText!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DeepNavy,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Trending AI Briefs
        Text(
            text = "Trending India AI Briefs",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepNavy
        )

        Spacer(modifier = Modifier.height(10.dp))

        val presetBriefs = listOf(
            "IndiaAI Mission: GPU & Sovereign LLMs" to "Approved ₹10,372 Cr for national compute & multi-language AI models.",
            "Semiconductor Fabrication in Gujarat" to "Dholera Fab plant ground-breaking under Semicon India scheme.",
            "ISRO Gaganyaan Crew Module Milestones" to "Pad Abort and Parachute recovery flight test successfully verified."
        )

        presetBriefs.forEach { (title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGrayBg)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Brief",
                        tint = SaffronPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DeepNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
