package com.example.data.repository

import com.example.data.db.TranslationDao
import com.example.data.model.TranslationHistory
import com.example.data.model.DownloadedPack
import com.example.data.model.QuickPhrase
import com.example.services.AiTranslationResult
import com.example.services.GeminiTranslator
import com.example.services.OfflineTranslator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TranslationRepository(private val dao: TranslationDao) {

    val allHistory: Flow<List<TranslationHistory>> = dao.getAllHistory()
    val favorites: Flow<List<TranslationHistory>> = dao.getFavorites()
    val allPacks: Flow<List<DownloadedPack>> = dao.getAllPacks()

    fun getQuickPhrases(category: String, targetLang: String): Flow<List<QuickPhrase>> =
        dao.getQuickPhrases(category, targetLang)

    suspend fun insertHistory(item: TranslationHistory) = withContext(Dispatchers.IO) {
        dao.insertHistory(item)
    }

    suspend fun toggleFavorite(id: Long, isFav: Boolean) = withContext(Dispatchers.IO) {
        dao.toggleFavorite(id, isFav)
    }

    suspend fun deleteHistory(item: TranslationHistory) = withContext(Dispatchers.IO) {
        dao.deleteHistory(item)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearHistory()
    }

    suspend fun updateDownloadState(code: String, downloaded: Boolean, downloading: Boolean, progress: Int) =
        withContext(Dispatchers.IO) {
            dao.updatePackDownloadState(code, downloaded, downloading, progress)
        }

    suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String,
        isAiOnlineMode: Boolean,
        isVoice: Boolean = false,
        isCamera: Boolean = false
    ): AiTranslationResult = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext AiTranslationResult("", null, null, null, emptyList())
        }

        val result = if (isAiOnlineMode) {
            // Online mode: use Gemini Translator
            GeminiTranslator.translateWithAi(text, sourceLang, targetLang)
        } else {
            // Offline mode: use fast Offline Translator
            val localTranslated = OfflineTranslator.translateOffline(text, sourceLang, targetLang)
            AiTranslationResult(
                translation = localTranslated,
                grammarImprovement = null,
                contextNotes = "Offline Pack Active | Instant Response",
                slangDetected = null,
                phraseSuggestions = emptyList()
            )
        }

        // Save translation into history
        val historyItem = TranslationHistory(
            sourceText = text,
            translatedText = result.translation,
            sourceLang = sourceLang,
            targetLang = targetLang,
            isVoice = isVoice,
            isCamera = isCamera,
            contextNotes = if (isAiOnlineMode) {
                var notes = ""
                result.grammarImprovement?.let { notes += "💡 Grammar: $it\n" }
                result.slangDetected?.let { notes += "🌶️ Slang: $it\n" }
                result.contextNotes?.let { notes += "🗣️ Context: $it" }
                notes.trim().ifEmpty { null }
            } else {
                result.contextNotes
            }
        )
        dao.insertHistory(historyItem)

        return@withContext result
    }
}
