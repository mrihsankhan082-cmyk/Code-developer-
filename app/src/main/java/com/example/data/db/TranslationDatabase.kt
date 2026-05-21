package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.TranslationHistory
import com.example.data.model.DownloadedPack
import com.example.data.model.QuickPhrase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TranslationHistory::class, DownloadedPack::class, QuickPhrase::class],
    version = 1,
    exportSchema = false
)
abstract class TranslationDatabase : RoomDatabase() {
    abstract val dao: TranslationDao

    companion object {
        @Volatile
        private var INSTANCE: TranslationDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): TranslationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TranslationDatabase::class.java,
                    "translation_db"
                )
                .addCallback(DatabasePrepCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabasePrepCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.dao)
                }
            }
        }

        private suspend fun populateInitialData(dao: TranslationDao) {
            // Populate Language Packs
            val initialPacks = listOf(
                DownloadedPack("en", "English", 0.0, true),
                DownloadedPack("ur", "Urdu", 42.5),
                DownloadedPack("es", "Spanish", 28.1, true), // let's pre-download Spanish and English!
                DownloadedPack("ar", "Arabic", 38.6),
                DownloadedPack("fr", "French", 31.4),
                DownloadedPack("de", "German", 33.2),
                DownloadedPack("zh", "Chinese", 45.1),
                DownloadedPack("hi", "Hindi", 41.2),
                DownloadedPack("ps", "Pashto", 35.8),
                DownloadedPack("tr", "Turkish", 29.5),
                DownloadedPack("ru", "Russian", 39.8),
                DownloadedPack("ja", "Japanese", 42.9),
                DownloadedPack("ko", "Korean", 36.4)
            )
            dao.insertPacks(initialPacks)

            // Populate some common quick phrases for travel
            val quickPhrases = mutableListOf<QuickPhrase>()
            val languages = listOf("es", "ur", "ar", "fr", "de", "zh", "hi", "ps", "tr", "ru", "ja", "ko")

            // Categories: Hotel, Airport, Taxi, Restaurant, Shopping, Medical
            for (lang in languages) {
                when (lang) {
                    "es" -> {
                        quickPhrases.add(QuickPhrase(category = "Hotel", englishText = "I have a reservation under my name.", targetLang = lang, translatedText = "Tengo una reserva a mi nombre.", phoneticText = "Ten-go oo-na re-ser-va a mee nom-bre"))
                        quickPhrases.add(QuickPhrase(category = "Hotel", englishText = "Is breakfast included?", targetLang = lang, translatedText = "¿El desayuno está incluido?", phoneticText = "El de-sa-yoo-no es-tah een-cloo-ee-do"))
                        quickPhrases.add(QuickPhrase(category = "Airport", englishText = "Where is the baggage claim?", targetLang = lang, translatedText = "¿Dónde está el reclamo de equipaje?", phoneticText = "Don-de es-tah el re-clah-mo de eh-kee-pah-heh"))
                        quickPhrases.add(QuickPhrase(category = "Taxi", englishText = "Please take me to this address.", targetLang = lang, translatedText = "Por favor, lléveme a esta dirección.", phoneticText = "Por fah-vor, yeh-veh-meh a es-tah dee-rek-syohn"))
                        quickPhrases.add(QuickPhrase(category = "Restaurant", englishText = "Can I have the menu, please?", targetLang = lang, translatedText = "¿Me trae el menú, por favor?", phoneticText = "Meh trah-eh el meh-noo por fah-vor"))
                        quickPhrases.add(QuickPhrase(category = "Shopping", englishText = "How much does this cost?", targetLang = lang, translatedText = "¿Cuánto cuesta esto?", phoneticText = "Kwan-to kwes-tah es-to"))
                        quickPhrases.add(QuickPhrase(category = "Medical", englishText = "I need medical help.", targetLang = lang, translatedText = "Necesito ayuda médica.", phoneticText = "Ne-ce-see-to ah-yoo-da meh-dee-ka"))
                    }
                    "ur" -> {
                        quickPhrases.add(QuickPhrase(category = "Hotel", englishText = "I have a reservation under my name.", targetLang = lang, translatedText = "میرے نام پر بکنگ ہے۔", phoneticText = "Mere naam par booking hai."))
                        quickPhrases.add(QuickPhrase(category = "Hotel", englishText = "Is breakfast included?", targetLang = lang, translatedText = "کیا ناشتہ اس میں شامل ہے؟", phoneticText = "Kya nashta is mein shamil hai?"))
                        quickPhrases.add(QuickPhrase(category = "Airport", englishText = "Where is the baggage claim?", targetLang = lang, translatedText = "سامان وصول کرنے کی جگہ کہاں ہے؟", phoneticText = "Saman vasool karne ki jagah kahan hai?"))
                        quickPhrases.add(QuickPhrase(category = "Taxi", englishText = "Please take me to this address.", targetLang = lang, translatedText = "براہ کرم مجھے اس پتے پر لے جائیں۔", phoneticText = "Baraye meharbani mujhe is pate par le jayen."))
                        quickPhrases.add(QuickPhrase(category = "Restaurant", englishText = "Can I have the menu, please?", targetLang = lang, translatedText = "کیا مجھے مینو مل سکتا ہے؟", phoneticText = "Kya mujhe menu mil sakta hai?"))
                        quickPhrases.add(QuickPhrase(category = "Shopping", englishText = "How much does this cost?", targetLang = lang, translatedText = "اس کی قیمت کیا ہے؟", phoneticText = "Is ki qeemat kya hai?"))
                        quickPhrases.add(QuickPhrase(category = "Medical", englishText = "I need medical help.", targetLang = lang, translatedText = "مجھے طبی امداد کی ضرورت ہے۔", phoneticText = "Mujhe tibbi imdad ki zaroorat hai."))
                    }
                    else -> {
                        // Populate generic fallbacks for testing the dropdown category phrase lists
                        quickPhrases.add(QuickPhrase(category = "Hotel", englishText = "I have a reservation under my name.", targetLang = lang, translatedText = "Reservation translation [${lang.uppercase()}]", phoneticText = "Pronunciation guide"))
                        quickPhrases.add(QuickPhrase(category = "Hotel", englishText = "Is breakfast included?", targetLang = lang, translatedText = "Breakfast included? [${lang.uppercase()}]", phoneticText = "Breakfast guide"))
                        quickPhrases.add(QuickPhrase(category = "Airport", englishText = "Where is the baggage claim?", targetLang = lang, translatedText = "Baggage claim? [${lang.uppercase()}]", phoneticText = "Baggage guide"))
                        quickPhrases.add(QuickPhrase(category = "Taxi", englishText = "Please take me to this address.", targetLang = lang, translatedText = "Take me to address [${lang.uppercase()}]", phoneticText = "Taxi address guide"))
                        quickPhrases.add(QuickPhrase(category = "Restaurant", englishText = "Can I have the menu, please?", targetLang = lang, translatedText = "Menu please [${lang.uppercase()}]", phoneticText = "Menu guide"))
                        quickPhrases.add(QuickPhrase(category = "Shopping", englishText = "How much does this cost?", targetLang = lang, translatedText = "How much? [${lang.uppercase()}]", phoneticText = "Price guide"))
                        quickPhrases.add(QuickPhrase(category = "Medical", englishText = "I need medical help.", targetLang = lang, translatedText = "Medical emergency [${lang.uppercase()}]", phoneticText = "Emergency guide"))
                    }
                }
            }
            dao.insertQuickPhrases(quickPhrases)
        }
    }
}
