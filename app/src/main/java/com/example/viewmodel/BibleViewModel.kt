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
import org.json.JSONArray
import org.json.JSONObject

class BibleViewModel(application: Application) : AndroidViewModel(application) {
    
    var isLoading by mutableStateOf(true)
        private set
    var books by mutableStateOf<List<Book>>(emptyList())
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    init {
        loadBible()
    }
    
    private fun loadBible() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                Log.d("SOZO", "Loading Bible start...")
                
                val loadedBooks = withContext(Dispatchers.IO) {
                    val context = getApplication<Application>()
                    
                    // Step 1: KJV file padho
                    Log.d("SOZO", "Reading KJV JSON...")
                    val kjvText = context.assets.open("bible_kjv.json").bufferedReader().use { it.readText() }
                    val kjvJson = JSONObject(kjvText)
                    
                    // Step 2: UGV file padho  
                    Log.d("SOZO", "Reading UGV JSON...")
                    val ugvText = context.assets.open("bible_urdu.json").bufferedReader().use { it.readText() }
                    val ugvJson = JSONObject(ugvText)
                    
                    Log.d("SOZO", "Parsing & merging 31k verses...")
                    
                    // Step 3: Dono ko merge karo
                    mergeBibles(kjvJson, ugvJson)
                }
                
                books = loadedBooks
                isLoading = false
                Log.d("SOZO", "SUCCESS! Loaded ${books.size} books")
                
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
        
        // Har book ke liye loop
        for (i in 0 until kjvBooks.length()) {
            val kjvBook = kjvBooks.getJSONObject(i)
            val ugvBook = ugvBooks.getJSONObject(i)
            
            val bookName = kjvBook.getString("name")
            val kjvChapters = kjvBook.getJSONArray("chapters")
            val ugvChapters = ugvBook.getJSONArray("chapters")
            
            val chapterList = mutableListOf<Chapter>()
            
            // Har chapter ke liye
            for (j in 0 until kjvChapters.length()) {
                val kjvChapter = kjvChapters.getJSONObject(j)
                val ugvChapter = ugvChapters.getJSONObject(j)
                
                val chapterNum = kjvChapter.getInt("chapter")
                val kjvVerses = kjvChapter.getJSONArray("verses")
                val ugvVerses = ugvChapter.getJSONArray("verses")
                
                val verseList = mutableListOf<Verse>()
                
                // Har verse ke liye
                for (k in 0 until kjvVerses.length()) {
                    val kjvVerse = kjvVerses.getJSONObject(k)
                    val ugvVerse = ugvVerses.getJSONObject(k)
                    
                    val verseNum = kjvVerse.getInt("v")
                    val english = kjvVerse.getString("en")
                    val urdu = ugvVerse.getString("ur")
                    
                    verseList.add(Verse(verseNum, english, urdu))
                }
                
                chapterList.add(Chapter(chapterNum, verseList))
            }
            
            bookList.add(Book(bookName, chapterList))
            Log.d("SOZO", "Loaded book: $bookName")
        }
        
        return bookList
    }
}

// Data Classes
data class Book(val name: String, val chapters: List<Chapter>)
data class Chapter(val number: Int, val verses: List<Verse>)  
data class Verse(val number: Int, val english: String, val urdu: String)
