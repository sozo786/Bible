package com.example.data

import android.content.Context
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BibleRepository(private val context: Context) {

    // Combined Bible representation in memory
    private val books = ArrayList<BookDataCombined>()

    class BookDataCombined(
        val nameEnglish: String,
        val chapters: List<ChapterDataCombined>
    )

    class ChapterDataCombined(
        val chapter: Int,
        val verses: List<VerseDataCombined>
    )

    class VerseDataCombined(
        val v: Int,
        val en: String,
        val ur: String
    )

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Try to load bible_kjv.json and bible_urdu.json from assets root
                val enStream = context.assets.open("bible_kjv.json")
                val enJsonString = enStream.bufferedReader().use { it.readText() }
                val enObj = JSONObject(enJsonString)
                val enBooksArray = enObj.getJSONArray("books")

                val urStream = context.assets.open("bible_urdu.json")
                val urJsonString = urStream.bufferedReader().use { it.readText() }
                val urObj = JSONObject(urJsonString)
                val urBooksArray = urObj.getJSONArray("books")

                val tempBooks = ArrayList<BookDataCombined>()
                val minBooks = minOf(enBooksArray.length(), urBooksArray.length())

                for (bIdx in 0 until minBooks) {
                    val enBook = enBooksArray.getJSONObject(bIdx)
                    val urBook = urBooksArray.getJSONObject(bIdx)

                    val enBookName = enBook.getString("name")
                    val enChaptersArray = enBook.getJSONArray("chapters")
                    val urChaptersArray = urBook.getJSONArray("chapters")

                    val combinedChapters = ArrayList<ChapterDataCombined>()
                    val minChapters = minOf(enChaptersArray.length(), urChaptersArray.length())

                    for (cIdx in 0 until minChapters) {
                        val enChapter = enChaptersArray.getJSONObject(cIdx)
                        val urChapter = urChaptersArray.getJSONObject(cIdx)

                        val chapterNum = enChapter.getInt("chapter")
                        val enVersesArray = enChapter.getJSONArray("verses")
                        val urVersesArray = urChapter.getJSONArray("verses")

                        val combinedVerses = ArrayList<VerseDataCombined>()
                        val minVerses = minOf(enVersesArray.length(), urVersesArray.length())

                        for (vIdx in 0 until minVerses) {
                            val enVerse = enVersesArray.getJSONObject(vIdx)
                            val urVerse = urVersesArray.getJSONObject(vIdx)

                            val verseNum = enVerse.getInt("v")
                            val enText = enVerse.optString("en", "")
                            val urText = urVerse.optString("ur", "")

                            combinedVerses.add(
                                VerseDataCombined(
                                    v = verseNum,
                                    en = enText,
                                    ur = urText
                                )
                            )
                        }
                        combinedChapters.add(
                            ChapterDataCombined(
                                chapter = chapterNum,
                                verses = combinedVerses
                            )
                        )
                    }
                    tempBooks.add(
                        BookDataCombined(
                            nameEnglish = enBookName,
                            chapters = combinedChapters
                        )
                    )
                }

                synchronized(books) {
                    books.clear()
                    books.addAll(tempBooks)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to original files under bible/ folder
                loadFallback()
            }
        }
    }

    private fun loadFallback() {
        try {
            // Load original assets/bible/en_kjv.json
            val enStream = context.assets.open("bible/en_kjv.json")
            val enJsonString = enStream.bufferedReader().use { it.readText() }
            val enObj = JSONObject(enJsonString)
            val enBooksArray = enObj.getJSONArray("books")

            // Load original assets/bible/ur.json
            val urStream = context.assets.open("bible/ur.json")
            val urJsonString = urStream.bufferedReader().use { it.readText() }
            val urObj = JSONObject(urJsonString)
            val urBooksArray = urObj.getJSONArray("books")

            val tempBooks = ArrayList<BookDataCombined>()
            val minBooks = minOf(enBooksArray.length(), urBooksArray.length())

            for (bIdx in 0 until minBooks) {
                val enBook = enBooksArray.getJSONObject(bIdx)
                val urBook = urBooksArray.getJSONObject(bIdx)

                val enBookName = enBook.getString("name")
                val enChaptersArray = enBook.getJSONArray("chapters")
                val urChaptersArray = urBook.getJSONArray("chapters")

                val combinedChapters = ArrayList<ChapterDataCombined>()
                val minChapters = minOf(enChaptersArray.length(), urChaptersArray.length())

                for (cIdx in 0 until minChapters) {
                    val enChapterVerses = enChaptersArray.getJSONArray(cIdx)
                    val urChapterVerses = urChaptersArray.getJSONArray(cIdx)

                    val combinedVerses = ArrayList<VerseDataCombined>()
                    val minVerses = minOf(enChapterVerses.length(), urChapterVerses.length())

                    for (vIdx in 0 until minVerses) {
                        val enText = enChapterVerses.getString(vIdx)
                        val urText = urChapterVerses.getString(vIdx)

                        combinedVerses.add(
                            VerseDataCombined(
                                v = vIdx + 1,
                                en = enText,
                                ur = urText
                            )
                        )
                    }
                    combinedChapters.add(
                        ChapterDataCombined(
                            chapter = cIdx + 1,
                            verses = combinedVerses
                        )
                    )
                }
                tempBooks.add(
                    BookDataCombined(
                        nameEnglish = enBookName,
                        chapters = combinedChapters
                    )
                )
            }

            synchronized(books) {
                books.clear()
                books.addAll(tempBooks)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getVerses(bookIndex: Int, chapterNumber: Int): List<Verse> {
        val book = books.getOrNull(bookIndex - 1) ?: return emptyList()
        val chapter = book.chapters.find { it.chapter == chapterNumber } ?: return emptyList()
        return chapter.verses.map {
            Verse(
                number = it.v,
                textUrdu = it.ur,
                textEnglish = it.en
            )
        }
    }

    fun search(query: String): List<SearchResult> {
        if (query.trim().length < 2) return emptyList()
        val results = mutableListOf<SearchResult>()
        
        books.forEachIndexed { bIdx, book ->
            val metadata = BibleData.getBookByIndex(bIdx + 1) ?: return@forEachIndexed
            book.chapters.forEach { chapter ->
                chapter.verses.forEach { verse ->
                    val matchEnglish = verse.en.contains(query, ignoreCase = true)
                    val matchUrdu = verse.ur.contains(query)
                    
                    if (matchEnglish || matchUrdu) {
                        results.add(
                            SearchResult(
                                bookIndex = bIdx + 1,
                                bookEnglishName = metadata.englishName,
                                bookUrduName = metadata.urduName,
                                chapterNumber = chapter.chapter,
                                verseNumber = verse.v,
                                textUrdu = verse.ur,
                                textEnglish = verse.en,
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
