package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateAiSummary(article: NewsArticle, language: String = "hi"): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val isHindi = language.lowercase().startsWith("hi") || language == "Hindi"
        val langInstruction = if (isHindi) "Write the 3-bullet summary in Hindi." else "Write the 3-bullet summary in English."
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateFallbackSummary(article, isHindi)
        }

        val prompt = """
            You are GenZ Bharat AI, an expert Indian news analyst.
            Analyze this article:
            Title: ${article.title}
            Description: ${article.description}
            Content: ${article.content}

            Provide a concise 3-bullet AI Summary focused on key facts for Indian readers. Keep it crisp and clear.
            $langInstruction
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt, apiKey)
            if (responseText.isNotBlank()) responseText else generateFallbackSummary(article, isHindi)
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Error calling Gemini API", e)
            generateFallbackSummary(article, isHindi)
        }
    }

    suspend fun translateArticle(article: NewsArticle, targetLanguage: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "[$targetLanguage Translation Mode]\n\n${article.title}\n\n${article.description}\n\n${article.content}"
        }

        val prompt = """
            Translate the following news story into $targetLanguage accurately and naturally for Indian readers:
            Headline: ${article.title}
            Summary: ${article.description}
            Content: ${article.content}
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt, apiKey)
            if (responseText.isNotBlank()) responseText else article.description
        } catch (e: Exception) {
            article.description
        }
    }

    suspend fun askAiQuestion(article: NewsArticle, question: String, language: String = "hi"): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val isHindi = language.lowercase().startsWith("hi") || language == "Hindi"
        val langInstruction = if (isHindi) "Answer the user's question in Hindi." else "Answer the user's question in English."
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext if (isHindi) {
                "'${article.title}' के संबंध में: $question\n\nAI विश्लेषण: वर्तमान भारतीय बाजार और नीतियों के आधार पर, यह घटनाक्रम स्थानीय बुनियादी ढांचे, क्षेत्रीय विकास और उपभोक्ता तकनीक को प्रभावित करता है।"
            } else {
                "Regarding '${article.title}': $question\n\nAI Insight: Based on current Indian market and policy context, this development impacts local infrastructure, regional growth, and consumer tech dynamics."
            }
        }

        val prompt = """
            Article Title: ${article.title}
            Article Context: ${article.content}
            User Question: $question
            $langInstruction

            Answer the user's question directly, objectively, and accurately in 2-3 sentences from an Indian news perspective.
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt, apiKey)
            if (responseText.isNotBlank()) responseText else if (isHindi) "इस समय AI उत्तर उत्पन्न करने में असमर्थ है।" else "Unable to generate AI answer at this moment."
        } catch (e: Exception) {
            "Error analyzing question: ${e.message}"
        }
    }

    private fun callGeminiApi(prompt: String, apiKey: String): String {
        val modelUrls = listOf(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey",
            "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=$apiKey"
        )

        val rootJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)
        }

        val requestBody = rootJson.toString().toRequestBody(jsonMediaType)

        for (url in modelUrls) {
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w("GeminiApiService", "Endpoint $url returned status: ${response.code}")
                        return@use
                    }
                    val responseBody = response.body?.string() ?: return@use
                    val responseJson = JSONObject(responseBody)
                    val candidates = responseJson.optJSONArray("candidates") ?: return@use
                    if (candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content") ?: return@use
                        val parts = content.optJSONArray("parts") ?: return@use
                        if (parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text", "")
                            if (text.isNotBlank()) return text
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GeminiApiService", "Exception calling endpoint $url", e)
            }
        }
        return ""
    }

    private fun getApiKey(): String {
        var key = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        if (key.isBlank() || key == "MY_GEMINI_API_KEY" || key.contains("YOUR_KEY")) {
            key = System.getenv("GEMINI_API_KEY") ?: ""
        }
        if (key == "MY_GEMINI_API_KEY" || key.contains("YOUR_KEY")) {
            return ""
        }
        return key
    }

    private fun generateFallbackSummary(article: NewsArticle, isHindi: Boolean = false): String {
        val summary = article.aiSummary
        if (!summary.isNullOrEmpty()) return summary
        return if (isHindi) {
            "• ${article.title}\n• ${article.category} एवं ${article.region} से संबंधित महत्वपूर्ण विवरण।\n• भारतीय समाचार दृष्टिकोण से प्रमुख प्रभाव।"
        } else {
            "• ${article.title}\n• Focuses on ${article.category} developments across ${article.region}.\n• Key impact on Indian economy and regional updates."
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
}
