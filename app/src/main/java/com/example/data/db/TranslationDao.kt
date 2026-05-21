package com.example.data.db

import androidx.room.*
import com.example.data.model.TranslationHistory
import com.example.data.model.DownloadedPack
import com.example.data.model.QuickPhrase
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    // History Queries
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<TranslationHistory>>

    @Query("SELECT * FROM translation_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<TranslationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: TranslationHistory)

    @Update
    suspend fun updateHistory(item: TranslationHistory)

    @Query("UPDATE translation_history SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFav: Boolean)

    @Delete
    suspend fun deleteHistory(item: TranslationHistory)

    @Query("DELETE FROM translation_history")
    suspend fun clearHistory()

    // Downloaded Pack Queries
    @Query("SELECT * FROM downloaded_packs")
    fun getAllPacks(): Flow<List<DownloadedPack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: DownloadedPack)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPacks(packs: List<DownloadedPack>)

    @Query("UPDATE downloaded_packs SET isDownloaded = :downloaded, isDownloading = :downloading, progress = :prog WHERE languageCode = :code")
    suspend fun updatePackDownloadState(code: String, downloaded: Boolean, downloading: Boolean, prog: Int)

    // Quick Phrases
    @Query("SELECT * FROM quick_phrases WHERE category = :cat AND targetLang = :lang")
    fun getQuickPhrases(cat: String, lang: String): Flow<List<QuickPhrase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickPhrases(phrases: List<QuickPhrase>)
}
