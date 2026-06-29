package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

enum class TranslationMode { ENGLISH, URDU, BOTH }

class BibleViewModel(application: Application) : AndroidViewModel(application) {
    
    var isLoading by mutableStateOf(true)
    var books by mutableStateOf<List<Book>>(emptyList())
    var errorMessage by mutableStateOf<String?>(null)
    
    // UI Variables
    var isDarkMode by mutableStateOf(false)
    var fontSize by mutableStateOf(16f)
    var translationMode by mutableStateOf(TranslationMode.BOTH)
    var isOnboardingCompleted by mutableStateOf(true)
    var lastReadBookIndex by mutableStateOf(0)
    var lastReadChapterNum by mutableStateOf(1)
    var currentBookIndex by mutableStateOf(0)
    var currentChapterNum by mutableStateOf(1)
    var currentVerses by mutableStateOf<List<Verse>>(emptyList())
    var verseOfTheDay by mutableStateOf<Verse?>(null)
    var bookmarks by mutableStateOf<List<Bookmark>>(emptyList())
    var searchQuery by mutableStateOf("")
    var searchResults by mutableStateOf<List<SearchResult>>(emptyList())
    var isSearching by mutableStateOf(false)
    
    init {
        loadBible()
    }
    
    // UI Functions
    fun toggleDarkMode() { isDarkMode = !isDarkMode }
    fun updateFontSize(newSize: Float) { fontSize = newSize }
    fun updateTranslationMode(mode: TranslationMode) { translationMode = mode }
    fun completeOnboarding() { isOnboardingCompleted = true }
    fun prevChapter() { if (currentChapterNum > 1) currentChapterNum-- }
    fun nextChapter() { currentChapterNum++ }
    fun navigateToChapter(bookIndex: Int, chapterNum: Int) { 
        currentBookIndex = bookIndex
        currentChapterNum = chapterNum
    }
    fun performSearch(query: String) { searchQuery = query }
    fun toggleBookmark(verse: Verse) { }
    fun deleteBookmark(id: String) { }
    fun isBookmarkedFlow(verse: Verse) = flowOf(false)
    fun triggerVerseNotification() { }
    
    private fun loadBible() {
        viewModelScope.launch {
            try {
                isLoading = true
                val loadedBooks = withContext(Dispatchers.IO) {
                    val context = getApplication<Application>()
                    val kjv = JSONObject(context.assets.open("bible_kjv.json").bufferedReader().use { it.readText() })
                    val ugv = JSONObject(context.assets.open("bible_urdu.json").bufferedReader().use { it.readText() })
                    val result = mutableListOf<Book>()
                    val kjvBooks = kjv.getJSONArray("books")
                    val ugvBooks = ugv.getJSONArray("books")
                    for (i in 0 until kjvBooks.length()) {
                        val kjvBook = kjvBooks.getJSONObject(i)
                        val ugvBook = ugvBooks.getJSONObject(i)
                        val chapters = mutableListOf<Chapter>()
                        val kjvChaps = kjvBook.getJSONArray("chapters")
                        val ugvChaps = ugvBook.getJSONArray("chapters")
                        for (j in 0 until kjvChaps.length()) {
                            val kjvChap = kjvChaps.getJSONObject(j)
                            val ugvChap = ugvChaps.getJSONObject(j)
                            val verses = mutableListOf<Verse>()
                            val kjvVers = kjvChap.getJSONArray("verses")
                            val ugvVers = ugvChap.getJSONArray("verses")
                            for (k in 0 until kjvVers.length()) {
                                verses.add(Verse(
                                    kjvVers.getJSONObject(k).getInt("v"),
                                    kjvVers.getJSONObject(k).getString("en"),
                                    ugvVers.getJSONObject(k).getString("ur")
                                ))
                            }
                            chapters.add(Chapter(kjvChap.getInt("chapter"), verses))
                        }
                        result.add(Book(kjvBook.getString("name"), chapters))
                    }
                    result
                }
                books = loadedBooks
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed: ${e.message}"
                isLoading = false
            }
        }
    }
}

data class Book(val name: String, val chapters: List<Chapter>)
data class Chapter(val number: Int, val verses: List<Verse>)  
data class Verse(val number: Int, val english: String, val urdu: String)
data class Bookmark(val id: String, val bookIndex: Int, val chapterNumber: Int, val verseNumber: Int, val textUrdu: String, val textEnglish: String, val bookUrduName: String, val bookEnglishName: String)
data class SearchResult(val bookIndex: Int, val chapterNumber: Int, val verseNumber: Int, val textUrdu: String, val textEnglish: String, val bookUrduName: String, val bookEnglishName: String)
