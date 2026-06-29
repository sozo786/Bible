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
    
    var isLoading by mutableStateOf(true)
        private set
    var books by mutableStateOf<List<Book>>(emptyList())
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var isDarkMode by mutableStateOf(false)
        private set
    var fontSize by mutableStateOf(16f)
        private set
    var translationMode by mutableStateOf(TranslationMode.BOTH)
        private set
    
    init {
        loadBible()
    }
    
    fun toggleDarkMode() { isDarkMode = !isDarkMode }
    fun updateFontSize(newSize: Float) { fontSize = newSize }
    fun updateTranslationMode(mode: TranslationMode) { translationMode = mode }
    
    private fun loadBible() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                Log.d("SOZO", "Loading REAL Bible from assets...")
                
                val loadedBooks = withContext(Dispatchers.IO) {
                    val context = getApplication<Application>()
                    
                    // YE LINES SAB SE IMPORTANT HAIN - REAL FILES READ KAR RAHI HAIN
                    Log.d("SOZO", "Opening bible_kjv.json from assets")
                    val kjvText = context.assets.open("bible_kjv.json").bufferedReader().use { it.readText() }
                    
                    Log.d("SOZO", "Opening bible_urdu.json from assets")
                    val ugvText = context.assets.open("bible_urdu.json").bufferedReader().use { it.readText() }
                    
                    Log.d("SOZO", "Parsing 31,102 verses... This will take 5-8 seconds")
                    val kjvJson = JSONObject(kjvText)
                    val ugvJson = JSONObject(ugvText)
                    
                    val result = mutableListOf<Book>()
                    val kjvBooks = kjvJson.getJSONArray("books")
                    val ugvBooks = ugvJson.getJSONArray("books")
                    
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
                        Log.d("SOZO", "Loaded: ${kjvBook.getString("name")} - ${chapters.size} chapters")
                    }
                    result
                }
                
                books = loadedBooks
                isLoading = false
                Log.d("SOZO", "SUCCESS! Total books loaded: ${books.size}")
                
            } catch (e: Exception) {
                Log.e("SOZO", "FAILED TO LOAD BIBLE", e)
                errorMessage = "Failed: ${e.message}"
                isLoading = false
            }
        }
    }
}

data class Book(val name: String, val chapters: List<Chapter>)
data class Chapter(val number: Int, val verses: List<Verse>)  
data class Verse(val number: Int, val english: String, val urdu: String)
