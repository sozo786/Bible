package com.example.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class BibleViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val repository = BibleRepository(context)
    private val db = BookmarkDatabase.getDatabase(context)
    private val dao = db.bookmarkDao()
    private val settings = SettingsManager(context)

    // Shared UI state flows
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isDarkMode = MutableStateFlow(settings.isDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _fontSize = MutableStateFlow(settings.fontSize)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _translationMode = MutableStateFlow(settings.translationMode) // "urdu", "english", "both"
    val translationMode: StateFlow<String> = _translationMode.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(settings.isOnboardingCompleted)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // Last Read Book/Chapter state for "Continue Reading"
    private val _lastReadBookIndex = MutableStateFlow(settings.lastBookIndex)
    val lastReadBookIndex: StateFlow<Int> = _lastReadBookIndex.asStateFlow()

    private val _lastReadChapterNum = MutableStateFlow(settings.lastChapterNum)
    val lastReadChapterNum: StateFlow<Int> = _lastReadChapterNum.asStateFlow()

    // Current Reading coordinates
    private val _currentBookIndex = MutableStateFlow(settings.lastBookIndex)
    val currentBookIndex: StateFlow<Int> = _currentBookIndex.asStateFlow()

    private val _currentChapterNum = MutableStateFlow(settings.lastChapterNum)
    val currentChapterNum: StateFlow<Int> = _currentChapterNum.asStateFlow()

    private val _currentVerses = MutableStateFlow<List<Verse>>(emptyList())
    val currentVerses: StateFlow<List<Verse>> = _currentVerses.asStateFlow()

    // Bookmarks list from Room Database
    val bookmarks: StateFlow<List<Bookmark>> = dao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search coordinates
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Verse of the Day (computed statically so it changes exactly once per day)
    val verseOfTheDay: StateFlow<VerseOfTheDayModel> = flow {
        emit(computeVerseOfTheDay())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultVerseOfTheDay())

    init {
        viewModelScope.launch {
            // Load Bible assets on background thread
            repository.initialize()
            _isInitialized.value = true
            loadCurrentVerses()
        }
    }

    // Load verses for current Book and Chapter
    fun navigateToChapter(bookIndex: Int, chapterNum: Int) {
        val sanitizedBookIndex = bookIndex.coerceIn(1, 66)
        val book = BibleData.getBookByIndex(sanitizedBookIndex) ?: return
        val sanitizedChapterNum = chapterNum.coerceIn(1, book.chaptersCount)
        
        _currentBookIndex.value = sanitizedBookIndex
        _currentChapterNum.value = sanitizedChapterNum
        
        // Save coordinates as Last Read
        settings.lastBookIndex = sanitizedBookIndex
        settings.lastChapterNum = sanitizedChapterNum
        _lastReadBookIndex.value = sanitizedBookIndex
        _lastReadChapterNum.value = sanitizedChapterNum
        
        loadCurrentVerses()
    }

    fun nextChapter() {
        val book = BibleData.getBookByIndex(_currentBookIndex.value) ?: return
        if (_currentChapterNum.value < book.chaptersCount) {
            navigateToChapter(_currentBookIndex.value, _currentChapterNum.value + 1)
        } else if (_currentBookIndex.value < 66) {
            navigateToChapter(_currentBookIndex.value + 1, 1)
        }
    }

    fun prevChapter() {
        if (_currentChapterNum.value > 1) {
            navigateToChapter(_currentBookIndex.value, _currentChapterNum.value - 1)
        } else if (_currentBookIndex.value > 1) {
            val prevBook = BibleData.getBookByIndex(_currentBookIndex.value - 1) ?: return
            navigateToChapter(_currentBookIndex.value - 1, prevBook.chaptersCount)
        }
    }

    private fun loadCurrentVerses() {
        _currentVerses.value = repository.getVerses(_currentBookIndex.value, _currentChapterNum.value)
    }

    // Toggle and setting methods
    fun toggleDarkMode() {
        val next = !_isDarkMode.value
        settings.isDarkMode = next
        _isDarkMode.value = next
    }

    fun updateFontSize(newSize: Float) {
        val clamped = newSize.coerceIn(14f, 24f)
        settings.fontSize = clamped
        _fontSize.value = clamped
    }

    fun updateTranslationMode(mode: String) {
        if (mode in listOf("urdu", "english", "both")) {
            settings.translationMode = mode
            _translationMode.value = mode
        }
    }

    fun completeOnboarding() {
        settings.isOnboardingCompleted = true
        _isOnboardingCompleted.value = true
    }

    // Bookmark actions
    fun toggleBookmark(bookIndex: Int, chapterNumber: Int, verseNumber: Int, textUrdu: String, textEnglish: String) {
        viewModelScope.launch {
            val book = BibleData.getBookByIndex(bookIndex) ?: return@launch
            val isBookmarked = dao.isBookmarked(bookIndex, chapterNumber, verseNumber)
            if (isBookmarked) {
                dao.deleteBookmarkByCoords(bookIndex, chapterNumber, verseNumber)
            } else {
                val bookmark = Bookmark(
                    bookIndex = bookIndex,
                    bookEnglishName = book.englishName,
                    bookUrduName = book.urduName,
                    chapterNumber = chapterNumber,
                    verseNumber = verseNumber,
                    textUrdu = textUrdu,
                    textEnglish = textEnglish
                )
                dao.insertBookmark(bookmark)
            }
        }
    }

    fun deleteBookmark(bookmarkId: Int) {
        viewModelScope.launch {
            dao.deleteBookmark(bookmarkId)
        }
    }

    suspend fun isBookmarked(bookIndex: Int, chapterNum: Int, verseNum: Int): Boolean {
        return withContext(Dispatchers.IO) {
            dao.isBookmarked(bookIndex, chapterNum, verseNum)
        }
    }

    fun isBookmarkedFlow(bookIndex: Int, chapterNum: Int, verseNum: Int): Flow<Boolean> {
        return dao.isBookmarkedFlow(bookIndex, chapterNum, verseNum)
    }

    // Search action
    fun performSearch(query: String) {
        _searchQuery.value = query
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            return
        }
        _isSearching.value = true
        viewModelScope.launch {
            val results = withContext(Dispatchers.Default) {
                repository.search(query)
            }
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    // Local Notification for Verse of the Day
    fun triggerVerseNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "sozo_bible_daily_verse"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Verse of the Day",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily inspirational verse from Noor-e-Kalaam at 8 AM"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val vod = computeVerseOfTheDay()
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar) // Standard calendar icon
            .setContentTitle("Noor-e-Kalaam Daily Verse")
            .setContentText("${vod.refUrdu} \n${vod.textUrdu}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("${vod.refUrdu}\n${vod.textUrdu}\n\n${vod.refEnglish}\n${vod.textEnglish}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(101, notification)
    }

    // Computes dynamic Verse of the Day based on calendar day
    private fun computeVerseOfTheDay(): VerseOfTheDayModel {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        val inspiringVerses = listOf(
            VerseOfTheDayModel(
                bookIndex = 19, chapterNum = 23, verseNum = 1,
                refUrdu = "زبور 23:1", refEnglish = "Psalm 23:1",
                textUrdu = "خُداوند میرا چوپان ہے؛ مجھے کمی نہ ہوگی۔",
                textEnglish = "The Lord is my shepherd; I shall not want."
            ),
            VerseOfTheDayModel(
                bookIndex = 43, chapterNum = 1, verseNum = 1,
                refUrdu = "یوحنا 1:1", refEnglish = "John 1:1",
                textUrdu = "اِبتدا میں کلام تھا اور کلام خُدا کے ساتھ تھا اور کلام خُدا تھا۔",
                textEnglish = "In the beginning was the Word, and the Word was with God, and the Word was God."
            ),
            VerseOfTheDayModel(
                bookIndex = 43, chapterNum = 3, verseNum = 16,
                refUrdu = "یوحنا 3:16", refEnglish = "John 3:16",
                textUrdu = "کیونکہ خُدا نے دُنیا سے ایسی محبت رکھی کہ اُس نے اپنا اکلوتا بیٹا بخش دیا تاکہ جو کوئی اُس پر ایمان لائے ہلاک نہ ہو بلکہ ہمیشہ کی زندگی پائے۔",
                textEnglish = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life."
            ),
            VerseOfTheDayModel(
                bookIndex = 19, chapterNum = 1, verseNum = 1,
                refUrdu = "زبور 1:1", refEnglish = "Psalm 1:1",
                textUrdu = "مُبارک ہے وہ شخص جو شریروں کی مصلحت پر نہیں چلتا اور گناہگاروں کی راہ میں کھڑا نہیں ہوتا اور ٹھٹھا بازوں کی مجلس میں نہیں بیٹھتا۔",
                textEnglish = "Blessed is the man that walketh not in the counsel of the ungodly, nor standeth in the way of sinners, nor sitteth in the seat of the scornful."
            ),
            VerseOfTheDayModel(
                bookIndex = 1, chapterNum = 1, verseNum = 1,
                refUrdu = "پیدائش 1:1", refEnglish = "Genesis 1:1",
                textUrdu = "اِبتدا میں خُدا نے آسمان اور زمین کو پَیدا کیا۔",
                textEnglish = "In the beginning God created the heaven and the earth."
            )
        )
        
        return inspiringVerses[dayOfYear % inspiringVerses.size]
    }

    private fun defaultVerseOfTheDay(): VerseOfTheDayModel {
        return VerseOfTheDayModel(
            bookIndex = 19, chapterNum = 23, verseNum = 1,
            refUrdu = "زبور 23:1", refEnglish = "Psalm 23:1",
            textUrdu = "خُداوند میرا چوپان ہے؛ مجھے کمی نہ ہوگی۔",
            textEnglish = "The Lord is my shepherd; I shall not want."
        )
    }
}

data class VerseOfTheDayModel(
    val bookIndex: Int,
    val chapterNum: Int,
    val verseNum: Int,
    val refUrdu: String,
    val refEnglish: String,
    val textUrdu: String,
    val textEnglish: String
)
