package com.sozo.noorekalaam

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

enum class TranslationMode { ENGLISH, URDU, BOTH }

class BibleViewModel(application: Application) : AndroidViewModel(application) {
    
    // UI States - Ye sab UI me use ho rahe hain
    var isLoading by mutableStateOf(true)
        private set
    var books by mutableStateOf<List<Book>>(emptyList())
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    // Settings - UI inko dhoond raha tha
    var isDarkMode by mutableStateOf(false)
        private set
    var fontSize by mutableStateOf(16f)
        private set
    var translationMode by mutableStateOf(TranslationMode.BOTH)
        private set
    
    init {
        loadBible()
    }
    
    // Functions - UI inko call kar raha tha
    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }
    
    fun updateFontSize(newSize: Float) {
        fontSize = newSize
    }
    
    fun updateTranslationMode(mode: TranslationMode) {
        translationMode = mode
    }
    
    private fun loadBible() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                Log.d("SOZO", "Loading Bible...")
                
                val loadedBooks = withContext(Dispatchers.IO) {
                    val context = getApplication<Application>()
                    
                    val kjvText = context.assets.open("bible_kjv.json").bufferedReader().use { it.readText() }
                    val kjvJson = JSONObject(kjvText)
                    
                    val ugvText = context.assets.open("bible_urdu.json").bufferedReader().use { it.readText() }
                    val ugvJson = JSONObject(ugvText)
                    
                    Log.d("SOZO", "Parsing 31k verses...")
                    mergeBibles(kjvJson, ugvJson)
                }
                
                books = loadedBooks
                isLoading = false
                Log.d("SOZO", "DONE! Loaded ${books.size} books")
                
            } catch (e: Exception) {
                Log.e("SOZO", "FAILED", e)
                errorMessage = "Error: ${e.message}"
                isLoading = false
            }
        }
    }
    
    private fun mergeBibles(kjvJson: JSONObject, ugvJson: JSONObject): List<Book> {
        val bookList = mutableListOf<Book>()
        val kjvBooks = kjvJson.getJSONArray("books")
        val ugvBooks = ugvJson.getJSONArray("books")
        
        for (i in 0 until kjvBooks.length()) {
            val kjvBook = kjvBooks.getJSONObject(i)
            val ugvBook = ugvBooks.getJSONObject(i)
            
            val bookName = kjvBook.getString("name")
            val kjvChapters = kjvBook.getJSONArray("chapters")
            val ugvChapters = ugvBook.getJSONArray("chapters")
            
            val chapterList = mutableListOf<Chapter>()
            
            for (j in 0 until kjvChapters.length()) {
                val kjvChapter = kjvChapters.getJSONObject(j)
                val ugvChapter = ugvChapters.getJSONObject(j)
                
                val chapterNum = kjvChapter.getInt("chapter")
                val kjvVerses = kjvChapter.getJSONArray("verses")
                val ugvVerses = ugvChapter.getJSONArray("verses")
                
                val verseList = mutableListOf<Verse>()
                
                for (k in 0 until kjvVerses.length()) {
                    val kjvVerse = kjvVerses.getJSONObject(k)
                    val ugvVerse = ugvVerses.getJSONObject(k)
                    
                    verseList.add(
                        Verse(
                            number = kjvVerse.getInt("v"),
                            english = kjvVerse.getString("en"),
                            urdu = ugvVerse.getString("ur")
                        )
                    )
                }
                chapterList.add(Chapter(chapterNum, verseList))
            }
            bookList.add(Book(bookName, chapterList))
        }
        return bookList
    }
}

// Data Classes
data class Book(val name: String, val chapters: List<Chapter>)
data class Chapter(val number: Int, val verses: List<Verse>)  
data class Verse(val number: Int, val english: String, val urdu: String)
