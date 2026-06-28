package com.example.data

import android.content.Context
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BibleRepository(private val context: Context) {
    // Array indexing books from 1 to 66
    private val urduChaptersList = Array<List<List<String>>?>(67) { null }
    private val englishChaptersList = Array<List<List<String>>?>(67) { null }

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Load English KJV
                val enStream = context.assets.open("bible/en_kjv.json")
                val enJsonString = enStream.bufferedReader().use { it.readText() }
                val enObj = JSONObject(enJsonString)
                val enBooksArray = enObj.getJSONArray("books")
                
                for (i in 0 until enBooksArray.length()) {
                    val bookObj = enBooksArray.getJSONObject(i)
                    val bookName = bookObj.getString("name")
                    val chaptersArray = bookObj.getJSONArray("chapters")
                    val chapters = mutableListOf<List<String>>()
                    for (j in 0 until chaptersArray.length()) {
                        val chapterArray = chaptersArray.getJSONArray(j)
                        val verses = mutableListOf<String>()
                        for (k in 0 until chapterArray.length()) {
                            verses.add(chapterArray.getString(k))
                        }
                        chapters.add(verses)
                    }
                    
                    val metadata = BibleData.books.find { it.englishName.equals(bookName, ignoreCase = true) }
                    if (metadata != null) {
                        englishChaptersList[metadata.index] = chapters
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                // 2. Load Urdu Geo
                val urStream = context.assets.open("bible/ur.json")
                val urJsonString = urStream.bufferedReader().use { it.readText() }
                val urObj = JSONObject(urJsonString)
                val urBooksArray = urObj.getJSONArray("books")
                
                for (i in 0 until urBooksArray.length()) {
                    val bookObj = urBooksArray.getJSONObject(i)
                    val bookName = bookObj.getString("name")
                    val chaptersArray = bookObj.getJSONArray("chapters")
                    val chapters = mutableListOf<List<String>>()
                    for (j in 0 until chaptersArray.length()) {
                        val chapterArray = chaptersArray.getJSONArray(j)
                        val verses = mutableListOf<String>()
                        for (k in 0 until chapterArray.length()) {
                            verses.add(chapterArray.getString(k))
                        }
                        chapters.add(verses)
                    }
                    
                    val metadata = BibleData.books.find { it.urduName == bookName } ?: BibleData.books.find { it.englishName.equals(bookName, ignoreCase = true) } ?: BibleData.books.getOrNull(i)
                    if (metadata != null) {
                        urduChaptersList[metadata.index] = chapters
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getVerses(bookIndex: Int, chapterNumber: Int): List<Verse> {
        val englishChapters = englishChaptersList.getOrNull(bookIndex)
        val urduChapters = urduChaptersList.getOrNull(bookIndex)

        val englishVerses = englishChapters?.getOrNull(chapterNumber - 1) ?: emptyList()
        val urduVerses = urduChapters?.getOrNull(chapterNumber - 1) ?: emptyList()

        val maxVerses = maxOf(englishVerses.size, urduVerses.size)
        
        if (maxVerses == 0) {
            val book = BibleData.getBookByIndex(bookIndex) ?: return emptyList()
            return listOf(
                Verse(
                    number = 1,
                    textUrdu = "اِس کتاب یعنی ${book.urduName} کے باب ${chapterNumber} کو پڑھنے کے لیے خُداوند آپ کو فضل بخشے۔ (یہ باب جلد ہی دستیاب ہوگا)",
                    textEnglish = "May the Lord bless you as you read ${book.englishName} Chapter ${chapterNumber}. (Full offline content for this chapter is coming soon in the next release)"
                ),
                Verse(
                    number = 2,
                    textUrdu = "کلامِ پاک آپ کے قدموں کے لیے چراغ اور آپ کی راہ کے لیے روشنی ہو۔",
                    textEnglish = "Thy word is a lamp unto my feet, and a light unto my path. (Psalm 119:105)"
                )
            )
        }

        val list = mutableListOf<Verse>()
        for (i in 0 until maxVerses) {
            val enText = englishVerses.getOrNull(i) ?: "KJV verse translation details coming soon"
            val urText = urduVerses.getOrNull(i) ?: "اردو ترجمہ جلد ہی دستیاب ہوگا"
            list.add(Verse(number = i + 1, textUrdu = urText, textEnglish = enText))
        }
        return list
    }

    fun search(query: String): List<SearchResult> {
        if (query.trim().length < 2) return emptyList()
        val results = mutableListOf<SearchResult>()
        
        for (book in BibleData.books) {
            val enChapters = englishChaptersList.getOrNull(book.index) ?: emptyList()
            val urChapters = urduChaptersList.getOrNull(book.index) ?: emptyList()
            
            val maxChapters = maxOf(enChapters.size, urChapters.size)
            for (chIdx in 0 until maxChapters) {
                val enVerses = enChapters.getOrNull(chIdx) ?: emptyList()
                val urVerses = urChapters.getOrNull(chIdx) ?: emptyList()
                
                val maxVerses = maxOf(enVerses.size, urVerses.size)
                for (vIdx in 0 until maxVerses) {
                    val enText = enVerses.getOrNull(vIdx) ?: ""
                    val urText = urVerses.getOrNull(vIdx) ?: ""
                    
                    val matchEnglish = enText.contains(query, ignoreCase = true)
                    val matchUrdu = urText.contains(query)
                    
                    if (matchEnglish || matchUrdu) {
                        results.add(
                            SearchResult(
                                bookIndex = book.index,
                                bookEnglishName = book.englishName,
                                bookUrduName = book.urduName,
                                chapterNumber = chIdx + 1,
                                verseNumber = vIdx + 1,
                                textUrdu = urText,
                                textEnglish = enText,
                                matchInUrdu = matchUrdu
                            )
                        )
                    }
                }
            }
        }
        return results
    }
}

data class SearchResult(
    val bookIndex: Int,
    val bookEnglishName: String,
    val bookUrduName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val textUrdu: String,
    val textEnglish: String,
    val matchInUrdu: Boolean
)
