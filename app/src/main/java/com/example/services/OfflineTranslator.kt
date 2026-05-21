package com.example.services

import java.util.Locale

object OfflineTranslator {

    private val phrasesDictionary = mapOf(
        "hello" to mapOf(
            "ur" to "ہیلو / سلام", "es" to "Hola", "ar" to "مرحباً", "fr" to "Bonjour",
            "de" to "Hallo", "zh" to "你好 (Nǐ hǎo)", "hi" to "नमस्ते (Namaste)",
            "ps" to "سلام", "tr" to "Merhaba", "ru" to "Здравствуйте", "ja" to "こんにちは (Konnichiwa)",
            "ko" to "안녕하세요 (Annyeonghaseyo)"
        ),
        "good morning" to mapOf(
            "ur" to "صبح بخیر", "es" to "Buenos días", "ar" to "صباح الخير", "fr" to "Bonjour",
            "de" to "Guten Morgen", "zh" to "早上好 (Zǎoshang hǎo)", "hi" to "शुभ प्रभात (Shubh Prabhat)",
            "ps" to "سحر په خیر", "tr" to "Günaydın", "ru" to "Доброе утро", "ja" to "おはようございます",
            "ko" to "좋은 아침입니다"
        ),
        "how are you?" to mapOf(
            "ur" to "آپ کیسے ہیں؟", "es" to "¿Cómo estás?", "ar" to "كيف حالك؟", "fr" to "Comment ça va?",
            "de" to "Wie geht es dir?", "zh" to "你好吗？ (Nǐ hǎo ma?)", "hi" to "आप कैसे हैं? (Aap kaise hain?)",
            "ps" to "تاسو څنګه یاست؟", "tr" to "Nasılsın?", "ru" to "Как дела?", "ja" to "お元気ですか？",
            "ko" to "어떻게 지내세요?"
        ),
        "how are you" to mapOf(
            "ur" to "آپ کیسے ہیں؟", "es" to "¿Cómo estás?", "ar" to "كيف حالك؟", "fr" to "Comment ça va?",
            "de" to "Wie geht es dir?", "zh" to "你好吗？ (Nǐ hǎo ma?)", "hi" to "आप कैसे हैं? (Aap kaise hain?)",
            "ps" to "تاسو څنګه یاست؟", "tr" to "Nasılsın?", "ru" to "Как дела?", "ja" to "お元気ですか？",
            "ko" to "어떻게 지내세요?"
        ),
        "thank you" to mapOf(
            "ur" to "شکریہ", "es" to "Gracias", "ar" to "شكراً لك", "fr" to "Merci",
            "de" to "Danke", "zh" to "谢谢 (Xièxiè)", "hi" to "धन्यवाद (Dhanyavaad)",
            "ps" to "مننه", "tr" to "Teşekkür ederim", "ru" to "Спасибо", "ja" to "ありがとう (Arigatō)",
            "ko" to "감사합니다 (Gamsahabnida)"
        ),
        "where is the bathroom?" to mapOf(
            "ur" to "غسل خانہ کہاں ہے؟", "es" to "¿Dónde está el baño?", "ar" to "أين الحمام؟", "fr" to "Où sont les toilettes?",
            "de" to "Wo ist die Toilette?", "zh" to "洗手间在哪里？", "hi" to "शौचालय कहाँ है?",
            "ps" to "تشناب چیرته دی؟", "tr" to "Tuvalet nerede?", "ru" to "Где туалет?", "ja" to "トイレはどこですか？",
            "ko" to "화장실이 어디예요?"
        ),
        "i love you" to mapOf(
            "ur" to "میں آپ سے محبت کرتا ہوں", "es" to "Te amo", "ar" to "أنا أحبك", "fr" to "Je t'aime",
            "de" to "Ich liebe dich", "zh" to "我爱你 (Wǒ ài nǐ)", "hi" to "मैं तुमसे प्यार करता हूँ",
            "ps" to "زه ستا سره مینه لرم", "tr" to "Seni seviyorum", "ru" to "Я тебя люблю", "ja" to "愛しています",
            "ko" to "사랑해요"
        ),
        "where is the airport?" to mapOf(
            "ur" to "ہوائی اڈہ کہاں ہے؟", "es" to "¿Dónde está el aeropuerto?", "ar" to "أين المطار؟", "fr" to "Où est l'aéroport?",
            "de" to "Wo ist der Flughafen?", "zh" to "机场在哪里？", "hi" to "हवाई अड्डा कहाँ है?",
            "ps" to "هوایی ډګر چیرته دی؟", "tr" to "Havalimanı nerede?", "ru" to "Где аэропорт?", "ja" to "空港はどこですか？",
            "ko" to "공항이 어디인가요?"
        ),
        "how much is this?" to mapOf(
            "ur" to "یہ کتنے کا ہے؟", "es" to "¿Cuánto cuesta esto?", "ar" to "كم سعر هذا؟", "fr" to "Combien ça coûte?",
            "de" to "Wie viel kostet das?", "zh" to "这个多少钱？", "hi" to "यह कितने का है?",
            "ps" to "دا څو دی؟", "tr" to "Bu ne kadar?", "ru" to "Сколько это стоит?", "ja" to "これはいくらですか？",
            "ko" to "이것은 얼마인가요?"
        ),
        "i need help" to mapOf(
            "ur" to "مجھے مدد کی ضرورت ہے", "es" to "Necesito ayuda", "ar" to "أحتاج لمساعدة", "fr" to "J'ai besoin d'aide",
            "de" to "Ich brauche Hilfe", "zh" to "我需要帮助", "hi" to "मुझे मदद चाहिए",
            "ps" to "زه مرستې ته اړتیا لرم", "tr" to "Yardıma ihtiyacım var", "ru" to "Мне нужна помощь", "ja" to "助けてください",
            "ko" to "도움이 필요합니다"
        ),
        "welcomes" to mapOf(
            "ur" to "خوش آمدید", "es" to "De nada", "ar" to "على الرحب والسعة", "fr" to "De rien",
            "de" to "Bitte", "zh" to "不客气", "hi" to "आपका स्वागत है",
            "ps" to "ښه راغلاست", "tr" to "Rica ederim", "ru" to "Пожалуйста", "ja" to "どういたしまして",
            "ko" to "천만에요"
        ),
        "goodbye" to mapOf(
            "ur" to "خدا حافظ", "es" to "Adiós", "ar" to "مع السلامة", "fr" to "Au revoir",
            "de" to "Auf Wiedersehen", "zh" to "再见", "hi" to "अलविदा",
            "ps" to "د خدای پامان", "tr" to "Hoşça kal", "ru" to "До свидания", "ja" to "さようなら",
            "ko" to "안녕히 가세요"
        ),
        "yes" to mapOf(
            "ur" to "ہاں", "es" to "Sí", "ar" to "نعم", "fr" to "Oui",
            "de" to "Ja", "zh" to "是的", "hi" to "हाँ",
            "ps" to "هو", "tr" to "Evet", "ru" to "Да", "ja" to "はい",
            "ko" to "예"
        ),
        "no" to mapOf(
            "ur" to "نہیں", "es" to "No", "ar" to "لا", "fr" to "Non",
            "de" to "Nein", "zh" to "不", "hi" to "नहीं",
            "ps" to "نه", "tr" to "Hayır", "ru" to "Нет", "ja" to "いいえ",
            "ko" to "아니오"
        )
    )

    private val wordsDictionary = mapOf(
        "welcome" to mapOf("ur" to "خوش آمدید", "es" to "bienvenido", "ar" to "مرحباً", "fr" to "bienvenue", "de" to "willkommen", "zh" to "欢迎", "hi" to "स्वागत हे", "ps" to "ښه راغلاست", "tr" to "hoşgeldiniz", "ru" to "добро пожаловать", "ja" to "ようこそ", "ko" to "환영하다"),
        "airport" to mapOf("ur" to "ہوائی اڈہ", "es" to "aeropuerto", "ar" to "مطار", "fr" to "aéroport", "de" to "flughafen", "zh" to "机场", "hi" to "हवाई अड्डा", "ps" to "هوایی ډګر", "tr" to "havalimanı", "ru" to "аэропорт", "ja" to "空港", "ko" to "공항"),
        "hotel" to mapOf("ur" to "ہوٹل", "es" to "hotel", "ar" to "فندق", "fr" to "hôtel", "de" to "hotel", "zh" to "酒店", "hi" to "होटल", "ps" to "هوټل", "tr" to "otel", "ru" to "отель", "ja" to "ホテル", "ko" to "호텔"),
        "bathroom" to mapOf("ur" to "غسل خانہ", "es" to "baño", "ar" to "حمام", "fr" to "salle de bain", "de" to "badezimmer", "zh" to "浴室", "hi" to "बाथरूम", "ps" to "تشناب", "tr" to "banyo", "ru" to "ванная", "ja" to "トイレ", "ko" to "화장실"),
        "taxi" to mapOf("ur" to "ٹیکسی", "es" to "taxi", "ar" to "تاكسي", "fr" to "taxi", "de" to "taxi", "zh" to "出租车", "hi" to "टैक्सी", "ps" to "ټیکسي", "tr" to "taksi", "ru" to "такси", "ja" to "タクシー", "ko" to "택시"),
        "water" to mapOf("ur" to "پانی", "es" to "agua", "ar" to "ماء", "fr" to "eau", "de" to "wasser", "zh" to "水", "hi" to "पानी", "ps" to "اوبه", "tr" to "su", "ru" to "вода", "ja" to "水", "ko" to "물"),
        "food" to mapOf("ur" to "کھانا", "es" to "comida", "ar" to "طعام", "fr" to "nourriture", "de" to "essen", "zh" to "食物", "hi" to "भोजन", "ps" to "خواړه", "tr" to "yemek", "ru" to "еда", "ja" to "食べ物", "ko" to "음식"),
        "money" to mapOf("ur" to "پیسے", "es" to "dinero", "ar" to "نقود", "fr" to "argent", "de" to "geld", "zh" to "钱", "hi" to "पैसे", "ps" to "پیسې", "tr" to "para", "ru" to "деньги", "ja" to "お金", "ko" to "돈"),
        "doctor" to mapOf("ur" to "ڈاکٹر", "es" to "médico", "ar" to "طبيب", "fr" to "médecin", "de" to "arzt", "zh" to "医生", "hi" to "चिकित्सक", "ps" to "ډاکټر", "tr" to "doktor", "ru" to "врач", "ja" to "医者", "ko" to "의사"),
        "friend" to mapOf("ur" to "دوست", "es" to "amigo", "ar" to "صديق", "fr" to "ami", "de" to "freund", "zh" to "朋友", "hi" to "मित्र", "ps" to "ملګری", "tr" to "arkadaş", "ru" to "друг", "ja" to "友達", "ko" to "친구"),
        "where" to mapOf("ur" to "کہاں", "es" to "dónde", "ar" to "أين", "fr" to "où", "de" to "wo", "zh" to "哪里", "hi" to "कहाँ", "ps" to "چیرته", "tr" to "nerede", "ru" to "где", "ja" to "どこ", "ko" to "어디"),
        "how" to mapOf("ur" to "کیسے", "es" to "cómo", "ar" to "كيف", "fr" to "comment", "de" to "wie", "zh" to "如何", "hi" to "कैसे", "ps" to "څنګه", "tr" to "nasıl", "ru" to "как", "ja" to "どうやって", "ko" to "어떻게"),
        "price" to mapOf("ur" to "قیمت", "es" to "precio", "ar" to "سعر", "fr" to "prix", "de" to "preis", "zh" to "价格", "hi" to "कीमत", "ps" to "قیمت", "tr" to "fiyat", "ru" to "цена", "ja" to "価格", "ko" to "가격"),
        "love" to mapOf("ur" to "محبت", "es" to "amor", "ar" to "حب", "fr" to "amour", "de" to "liebe", "zh" to "爱", "hi" to "प्यार", "ps" to "مینه", "tr" to "aşk", "ru" to "любовь", "ja" to "愛", "ko" to "사랑"),
        "beautiful" to mapOf("ur" to "خوبصورت", "es" to "hermoso", "ar" to "جميل", "fr" to "beau", "de" to "schön", "zh" to "美丽", "hi" to "सुंदर", "ps" to "ښکلی", "tr" to "güzel", "ru" to "красивый", "ja" to "美しい", "ko" to "아름다운")
    )

    fun translateOffline(text: String, sourceLang: String, targetLang: String): String {
        val cleanText = text.trim().lowercase(Locale.ROOT).replace(Regex("[.?勘!,/|:]"), "")
        if (cleanText.isEmpty()) return ""

        // Case 1: Source and Target are the same
        if (sourceLang == targetLang) return text

        // Case 2: Exact phrase matching (from English to other)
        if (sourceLang == "en") {
            phrasesDictionary[cleanText]?.get(targetLang)?.let { return it }
        }

        // Exact phrase matching reverse (from other to English)
        if (targetLang == "en") {
            for ((engPhrase, translations) in phrasesDictionary) {
                if (translations[sourceLang]?.lowercase(Locale.ROOT) == cleanText) {
                    return engPhrase.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
            }
        }

        // Case 3: Word-by-word matching translation
        val words = text.split(" ")
        val translatedWords = words.map { word ->
            val cleanWord = word.lowercase(Locale.ROOT).replace(Regex("[^a-zA-Z]"), "")
            if (sourceLang == "en") {
                wordsDictionary[cleanWord]?.get(targetLang) ?: word
            } else if (targetLang == "en") {
                var found: String? = null
                for ((engWord, translations) in wordsDictionary) {
                    if (translations[sourceLang]?.lowercase(Locale.ROOT) == cleanWord) {
                        found = engWord
                        break
                    }
                }
                found ?: word
            } else {
                // Translator between two non-English languages (indirect translation via English)
                var englishIntermediate: String? = null
                for ((engWord, translations) in wordsDictionary) {
                    if (translations[sourceLang]?.lowercase(Locale.ROOT) == cleanWord) {
                        englishIntermediate = engWord
                        break
                    }
                }
                if (englishIntermediate != null) {
                    wordsDictionary[englishIntermediate]?.get(targetLang) ?: word
                } else {
                    word
                }
            }
        }

        // Format output nicely
        var result = translatedWords.joinToString(" ")
        if (result == text && sourceLang != "en") {
            // Local fallback simulation if words don't match exactly
            result = "[$targetLang] " + text + " (Offline translation simulation - Pack Active)"
        }
        return result.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun getLanguageName(code: String): String {
        return when (code) {
            "en" -> "English"
            "ur" -> "Urdu"
            "es" -> "Spanish"
            "ar" -> "Arabic"
            "fr" -> "French"
            "de" -> "German"
            "zh" -> "Chinese"
            "hi" -> "Hindi"
            "ps" -> "Pashto"
            "tr" -> "Turkish"
            "ru" -> "Russian"
            "ja" -> "Japanese"
            "ko" -> "Korean"
            else -> "Unknown"
        }
    }
}
