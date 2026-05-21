package com.example.ui.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.TranslationDatabase
import com.example.data.model.DownloadedPack
import com.example.data.model.QuickPhrase
import com.example.data.model.TranslationHistory
import com.example.data.repository.TranslationRepository
import com.example.services.AiTranslationResult
import com.example.services.OfflineTranslator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

enum class AppScreen {
    Splash,
    Onboarding,
    Home,
    VoiceTranslate,
    Conversation,
    CameraTranslate,
    LiveSubtitle,
    DownloadPacks,
    History,
    Favorites,
    TravelMode,
    Settings,
    Profile
}

class TranslationViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val db = TranslationDatabase.getInstance(application, viewModelScope)
    private val repository = TranslationRepository(db.dao)

    // Navigator State
    private val _currentScreen = MutableStateFlow(AppScreen.Splash)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Input/Output translation states
    val sourceLang = MutableStateFlow("en")
    val targetLang = MutableStateFlow("es")
    val sourceText = MutableStateFlow("")
    val isAutoDetect = MutableStateFlow(true)
    
    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _translationResult = MutableStateFlow<AiTranslationResult?>(null)
    val translationResult: StateFlow<AiTranslationResult?> = _translationResult.asStateFlow()

    // Database UI observation Flows
    val historyList: StateFlow<List<TranslationHistory>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritesList: StateFlow<List<TranslationHistory>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val languagePacks: StateFlow<List<DownloadedPack>> = repository.allPacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings States
    val isDarkTheme = MutableStateFlow(true)
    val useAiOnline = MutableStateFlow(true)
    val speechSpeed = MutableStateFlow(1.0f)
    val speechPitch = MutableStateFlow(1.0f)
    val voiceType = MutableStateFlow("AI Professional Female") // Female vs Male
    val subtitleSize = MutableStateFlow(18)
    val subtitleTransparency = MutableStateFlow(0.8f)

    // TTS Engine
    private var tts: TextToSpeech? = null
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    // Voice & Subtitle Screen states
    val isRecording = MutableStateFlow(false)
    val voiceWaveform = MutableStateFlow(emptyList<Float>())
    val spokenText = MutableStateFlow("")
    val subSpokenText = MutableStateFlow("Tap Microphone and start speaking...")
    val subTranslatedText = MutableStateFlow("")
    
    // Conversation Mode States
    val conversationHistory = MutableStateFlow(listOf<ConversationMessage>())
    val activeSpeaker = MutableStateFlow(0) // 0 for Top (User A), 1 for Bottom (User B)

    // Travel Mode Categories
    val selectedTravelCategory = MutableStateFlow("Hotel")
    val travelPhrases = selectedTravelCategory.flatMapLatest { cat ->
        targetLang.flatMapLatest { lang ->
            repository.getQuickPhrases(cat, lang)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Camera Mode Screen States
    val isFlashActive = MutableStateFlow(false)
    val cameraDetectionBox = MutableStateFlow<CameraDetection?>(null)

    init {
        // Initialize TTS
        tts = TextToSpeech(application, this)
        simulateWaveform()
        prepopulatePacksIfNeeded()
    }

    private fun prepopulatePacksIfNeeded() {
        // Handled by room database callback, but ensures standard packs flow has content.
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _isTtsReady.value = true
            tts?.setSpeechRate(speechSpeed.value)
            tts?.setPitch(speechPitch.value)
        } else {
            Log.e("TranslationViewModel", "TTS Initialization failed!")
        }
    }

    // Language list reference
    val supportedLanguages = listOf(
        LangItem("en", "English", "🇺🇸"),
        LangItem("ur", "Urdu", "🇵🇰"),
        LangItem("es", "Spanish", "🇪🇸"),
        LangItem("ar", "Arabic", "🇸🇦"),
        LangItem("fr", "French", "🇫🇷"),
        LangItem("de", "German", "🇩🇪"),
        LangItem("zh", "Chinese", "🇨🇳"),
        LangItem("hi", "Hindi", "🇮🇳"),
        LangItem("ps", "Pashto", "🇦🇫"),
        LangItem("tr", "Turkish", "🇹🇷"),
        LangItem("ru", "Russian", "🇷🇺"),
        LangItem("ja", "Japanese", "🇯🇵"),
        LangItem("ko", "Korean", "🇰🇷")
    )

    fun swapLanguages() {
        val temp = sourceLang.value
        sourceLang.value = targetLang.value
        targetLang.value = temp
        val tempText = sourceText.value
        if (translationResult.value != null) {
            sourceText.value = translationResult.value?.translation ?: ""
        }
    }

    // Trigger primary translation
    fun translateText() {
        val text = sourceText.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _isTranslating.value = true
            
            // Simulating Auto Language detection if turned on
            if (isAutoDetect.value) {
                detectLanguageOffline(text)
            }

            try {
                val result = repository.translate(
                    text = text,
                    sourceLang = sourceLang.value,
                    targetLang = targetLang.value,
                    isAiOnlineMode = useAiOnline.value,
                    isVoice = currentScreen.value == AppScreen.VoiceTranslate,
                    isCamera = currentScreen.value == AppScreen.CameraTranslate
                )
                _translationResult.value = result
            } catch (e: Exception) {
                Log.e("TranslationViewModel", "Unexpected translation error", e)
            } finally {
                _isTranslating.value = false
            }
        }
    }

    private fun detectLanguageOffline(text: String) {
        val lowerText = text.lowercase()
        // Heuristics for auto-detecting languages based on distinct characters or words
        when {
            lowerText.contains(Regex("[\\u0600-\\u06FF]")) -> {
                // Arabic / Urdu / Pashto
                when {
                    lowerText.contains(Regex("[پٹچڈڑژکگںےی]")) -> sourceLang.value = "ur"
                    lowerText.contains(Regex("[څځډږښړځڅ]")) -> sourceLang.value = "ps"
                    else -> sourceLang.value = "ar"
                }
            }
            lowerText.contains(Regex("[\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FBF]")) -> {
                // Japanese versus Chinese
                if (lowerText.contains(Regex("[の、はをにがてた]"))) sourceLang.value = "ja" else sourceLang.value = "zh"
            }
            lowerText.contains(Regex("[\\uAC00-\\uD7AF]")) -> sourceLang.value = "ko"
            lowerText.contains("hola") || lowerText.contains("gracias") || lowerText.contains("buenos") -> sourceLang.value = "es"
            lowerText.contains("bonjour") || lowerText.contains("merci") || lowerText.contains("s'il") -> sourceLang.value = "fr"
            lowerText.contains("hallo") || lowerText.contains("danke") || lowerText.contains("guten") -> sourceLang.value = "de"
            lowerText.contains(Regex("[\\u0400-\\u04FF]")) -> sourceLang.value = "ru"
            lowerText.contains(Regex("[\\u0900-\\u097F]")) -> sourceLang.value = "hi"
            lowerText.contains("merhaba") || lowerText.contains("teşekkür") -> sourceLang.value = "tr"
        }
    }

    // Play TTS
    fun speakText(text: String, langCode: String) {
        if (!_isTtsReady.value) return
        val locale = when (langCode) {
            "en" -> Locale.US
            "es" -> Locale.forLanguageTag("es-ES")
            "fr" -> Locale.FRANCE
            "de" -> Locale.GERMANY
            "zh" -> Locale.CHINESE
            "ja" -> Locale.JAPAN
            "ko" -> Locale.KOREA
            "ru" -> Locale.forLanguageTag("ru-RU")
            "hi" -> Locale.forLanguageTag("hi-IN")
            "ar" -> Locale.forLanguageTag("ar")
            "ur" -> Locale.forLanguageTag("ur")
            "tr" -> Locale.forLanguageTag("tr-TR")
            else -> Locale.US
        }
        
        tts?.setLanguage(locale)
        tts?.setSpeechRate(speechSpeed.value)
        tts?.setPitch(speechPitch.value)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TranslationEngine")
    }

    // Favorite history toggle
    fun toggleHistoryFavorite(history: TranslationHistory) {
        viewModelScope.launch {
            repository.toggleFavorite(history.id, !history.isFavorite)
        }
    }

    // Delete history
    fun deleteHistory(history: TranslationHistory) {
        viewModelScope.launch {
            repository.deleteHistory(history)
        }
    }

    // Clear history
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Simulator for wave heights on microphone
    private fun simulateWaveform() {
        viewModelScope.launch {
            while (true) {
                if (isRecording.value) {
                    val list = List(15) { (20..90).random().toFloat() }
                    voiceWaveform.value = list
                } else {
                    voiceWaveform.value = List(15) { 10f }
                }
                delay(120)
            }
        }
    }

    // Simulated Voice dictation action
    fun toggleVoiceRecording(isConversation: Boolean = false) {
        if (isRecording.value) {
            // Stop recording, trigger translation!
            isRecording.value = false
            
            val simulatedSpeech = when (sourceLang.value) {
                "ur" -> "آپ سے مل کر خوشی ہوئی۔"
                "es" -> "Hola, buenos días, ¿cómo está?"
                "fr" -> "Où est l'aéroport s'il vous plaît ?"
                "ar" -> "مرحباً، أين الفندق؟"
                else -> "Welcome to our beautiful hotel, is breakfast included?"
            }
            
            if (isConversation) {
                viewModelScope.launch {
                    val side = activeSpeaker.value
                    val sLang = if (side == 0) sourceLang.value else targetLang.value
                    val tLang = if (side == 0) targetLang.value else sourceLang.value
                    
                    val input = if (side == 0) simulatedSpeech else "Mucho gusto, estoy bien."
                    
                    val transRes = repository.translate(
                        text = input,
                        sourceLang = sLang,
                        targetLang = tLang,
                        isAiOnlineMode = useAiOnline.value,
                        isVoice = true
                    )
                    
                    val newMsg = ConversationMessage(
                        text = input,
                        translatedText = transRes.translation,
                        senderId = side,
                        sourceLang = sLang,
                        targetLang = tLang
                    )
                    conversationHistory.value = conversationHistory.value + newMsg
                    speakText(transRes.translation, tLang)
                    
                    // Toggle speaker side
                    activeSpeaker.value = if (side == 0) 1 else 0
                }
            } else {
                spokenText.value = simulatedSpeech
                sourceText.value = simulatedSpeech
                subSpokenText.value = simulatedSpeech
                translateText()
            }
        } else {
            // Start recording
            spokenText.value = ""
            isRecording.value = true
        }
    }

    fun addConversationMessage(text: String, transText: String, senderId: Int) {
        val sLang = if (senderId == 0) sourceLang.value else targetLang.value
        val tLang = if (senderId == 0) targetLang.value else sourceLang.value
        conversationHistory.value = conversationHistory.value + ConversationMessage(
            text = text,
            translatedText = transText,
            senderId = senderId,
            sourceLang = sLang,
            targetLang = tLang
        )
    }

    fun clearConversation() {
        conversationHistory.value = emptyList()
    }

    // Live Subtitles Simulation
    fun toggleLiveSubtitles() {
        if (isRecording.value) {
            isRecording.value = false
        } else {
            isRecording.value = true
            spokenText.value = ""
            viewModelScope.launch {
                val flowSpeech = listOf(
                    "Hello traveler...",
                    "Welcome to Berlin International Airport.",
                    "If you need help with bags, please ask any assistant.",
                    "Where is the bathroom situated?",
                    "Have a beautiful journey ahead!"
                )
                for (phrase in flowSpeech) {
                    if (!isRecording.value) break
                    subSpokenText.value = phrase
                    val translatedPhrase = OfflineTranslator.translateOffline(
                        phrase,
                        sourceLang.value,
                        targetLang.value
                    )
                    subTranslatedText.value = translatedPhrase
                    delay(3000)
                }
                isRecording.value = false
            }
        }
    }

    // Camera Mode Detection Simulators
    fun triggerCameraShot() {
        viewModelScope.launch {
            _isTranslating.value = true
            delay(1500) // simulation delay
            val sourceTextDetected = when (sourceLang.value) {
                "fr" -> "MENU DU JOUR - Café Gourmand 5€"
                "es" -> "RESTAURANTE EL CAMINO - Bienvenidos"
                "de" -> "ACHTUNG - Haltestelle Bahnhof"
                else -> "AIRPORT DEPARTURES - Terminal 1 Gates 12-24"
            }
            val translationRes = repository.translate(
                text = sourceTextDetected,
                sourceLang = sourceLang.value,
                targetLang = targetLang.value,
                isAiOnlineMode = useAiOnline.value,
                isCamera = true
            )
            cameraDetectionBox.value = CameraDetection(
                originalText = sourceTextDetected,
                translatedText = translationRes.translation,
                posX = 150f,
                posY = 280f,
                w = 550f,
                h = 160f
            )
            _isTranslating.value = false
        }
    }

    fun clearCameraDetection() {
        cameraDetectionBox.value = null
    }

    // Simulated downloadable package manager flow
    fun simulateDownloadPack(code: String) {
        viewModelScope.launch {
            repository.updateDownloadState(code, downloaded = false, downloading = true, progress = 0)
            for (p in 1..10) {
                delay(200)
                repository.updateDownloadState(code, downloaded = false, downloading = true, progress = p * 10)
            }
            repository.updateDownloadState(code, downloaded = true, downloading = false, progress = 100)
        }
    }

    fun simulateDeletePack(code: String) {
        viewModelScope.launch {
            repository.updateDownloadState(code, downloaded = false, downloading = false, progress = 0)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }
}

data class LangItem(val code: String, val name: String, val flag: String)
data class ConversationMessage(
    val text: String,
    val translatedText: String,
    val senderId: Int, // 0 for User A, 1 for User B
    val sourceLang: String,
    val targetLang: String
)
data class CameraDetection(
    val originalText: String,
    val translatedText: String,
    val posX: Float,
    val posY: Float,
    val w: Float,
    val h: Float
)
