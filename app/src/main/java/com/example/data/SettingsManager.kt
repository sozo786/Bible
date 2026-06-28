package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sozo_bible_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_TRANSLATION_LANG = "translation_lang" // "urdu", "english", "both"
        private const val KEY_LAST_BOOK_INDEX = "last_book_index"
        private const val KEY_LAST_CHAPTER_NUM = "last_chapter_num"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true) // Default to elegant Dark Mode!
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var fontSize: Float
        get() = prefs.getFloat(KEY_FONT_SIZE, 18f) // Default size 18
        set(value) = prefs.edit().putFloat(KEY_FONT_SIZE, value).apply()

    var translationMode: String
        get() = prefs.getString(KEY_TRANSLATION_LANG, "both") ?: "both" // "urdu", "english", "both"
        set(value) = prefs.edit().putString(KEY_TRANSLATION_LANG, value).apply()

    var lastBookIndex: Int
        get() = prefs.getInt(KEY_LAST_BOOK_INDEX, 1) // Default to Paidash (1)
        set(value) = prefs.edit().putInt(KEY_LAST_BOOK_INDEX, value).apply()

    var lastChapterNum: Int
        get() = prefs.getInt(KEY_LAST_CHAPTER_NUM, 1) // Default to chapter 1
        set(value) = prefs.edit().putInt(KEY_LAST_CHAPTER_NUM, value).apply()

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
}
