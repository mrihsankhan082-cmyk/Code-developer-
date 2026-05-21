package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TranslationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isVoice: Boolean = false,
    val isCamera: Boolean = false,
    val contextNotes: String? = null // AI Grammar notes or context explanations
)

@Entity(tableName = "downloaded_packs")
data class DownloadedPack(
    @PrimaryKey val languageCode: String,
    val languageName: String,
    val sizeMb: Double,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val progress: Int = 0
)

@Entity(tableName = "quick_phrases")
data class QuickPhrase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // e.g., Hotel, Airport, Taxi, Restaurant, Shopping, Medical
    val englishText: String,
    val targetLang: String,
    val translatedText: String,
    val phoneticText: String
)
