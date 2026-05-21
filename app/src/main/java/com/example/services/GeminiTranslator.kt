package com.example.services

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = 0.3f,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

// The structured data we expect back from our AI model
data class AiTranslationResult(
    val translation: String,
    val grammarImprovement: String?,
    val contextNotes: String?,
    val slangDetected: String?,
    val phraseSuggestions: List<String>
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiTranslator {
    private const val TAG = "GeminiTranslator"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun translateWithAi(
        text: String,
        sourceLang: String,
        targetLang: String
    ): AiTranslationResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured.")
            return getFallbackResult(text, sourceLang, targetLang, "API Key Missing/Not Set")
        }

        // Request a structured response from Gemini
        val prompt = """
            Translate the following text from $sourceLang to $targetLang:
            "$text"
            
            Provide the output in a clean, structured text block using double dollar signs ($$) as custom markers so that we can parse it easily:
            TRANS::[The primary translation strictly in the target language]
            GRAMMAR::[Any grammar improvement suggestions or write 'None']
            CONTEXT::[Contextual travel-friendly usage nuances or notes or write 'None']
            SLANG::[Slang/idiom detection notes or write 'None']
            SUGGESTIONS::[2 related phrase suggestions in the target language, separated by commas]
        """.trimIndent()

        val systemInstructionText = """
            You are an advanced AI translator. Keep translations natural, polite, and contextual. 
            Ensure you always adhere to the custom $$ markers format requested.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.2f),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstructionText)))
        )

        return try {
            val response = api.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                parseAiResponse(responseText, text, sourceLang, targetLang)
            } else {
                getFallbackResult(text, sourceLang, targetLang, "Empty AI response")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Translation error: ${e.message}", e)
            getFallbackResult(text, sourceLang, targetLang, "Network connection offline / error: ${e.localizedMessage}")
        }
    }

    private fun parseAiResponse(
        rawText: String,
        originalText: String,
        sourceLang: String,
        targetLang: String
    ): AiTranslationResult {
        var translation = ""
        var grammar: String? = null
        var context: String? = null
        var slang: String? = null
        var suggestions = listOf<String>()

        val lines = rawText.split("\n")
        for (line in lines) {
            when {
                line.startsWith("TRANS::") -> {
                    translation = line.substringAfter("TRANS::").trim()
                }
                line.startsWith("GRAMMAR::") -> {
                    val valStr = line.substringAfter("GRAMMAR::").trim()
                    if (valStr.lowercase() != "none") {
                        grammar = valStr
                    }
                }
                line.startsWith("CONTEXT::") -> {
                    val valStr = line.substringAfter("CONTEXT::").trim()
                    if (valStr.lowercase() != "none") {
                        context = valStr
                    }
                }
                line.startsWith("SLANG::") -> {
                    val valStr = line.substringAfter("SLANG::").trim()
                    if (valStr.lowercase() != "none") {
                        slang = valStr
                    }
                }
                line.startsWith("SUGGESTIONS::") -> {
                    val valStr = line.substringAfter("SUGGESTIONS::").trim()
                    if (valStr.lowercase() != "none") {
                        suggestions = valStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    }
                }
            }
        }

        // If something went wrong during parsing, fall back to offline translation but keep formatting
        if (translation.isEmpty()) {
            val simple = OfflineTranslator.translateOffline(originalText, sourceLang, targetLang)
            return AiTranslationResult(
                translation = simple,
                grammarImprovement = null,
                contextNotes = "Structured parsing failed; retrieved using offline backup rules.",
                slangDetected = null,
                phraseSuggestions = emptyList()
            )
        }

        return AiTranslationResult(
            translation = translation,
            grammarImprovement = grammar,
            contextNotes = context,
            slangDetected = slang,
            phraseSuggestions = suggestions
        )
    }

    private fun getFallbackResult(
        text: String,
        sourceLang: String,
        targetLang: String,
        reason: String
    ): AiTranslationResult {
        val result = OfflineTranslator.translateOffline(text, sourceLang, targetLang)
        return AiTranslationResult(
            translation = result,
            grammarImprovement = null,
            contextNotes = "Translated offline ($reason)",
            slangDetected = null,
            phraseSuggestions = emptyList()
        )
    }
}
