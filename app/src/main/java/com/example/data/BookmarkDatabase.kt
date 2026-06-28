package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookIndex: Int,
    val bookEnglishName: String,
    val bookUrduName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val textUrdu: String,
    val textEnglish: String,
    val dateAdded: Long = System.currentTimeMillis()
)

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY dateAdded DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Int)

    @Query("DELETE FROM bookmarks WHERE bookIndex = :bookIndex AND chapterNumber = :chapterNumber AND verseNumber = :verseNumber")
    suspend fun deleteBookmarkByCoords(bookIndex: Int, chapterNumber: Int, verseNumber: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE bookIndex = :bookIndex AND chapterNumber = :chapterNumber AND verseNumber = :verseNumber LIMIT 1)")
    fun isBookmarkedFlow(bookIndex: Int, chapterNumber: Int, verseNumber: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE bookIndex = :bookIndex AND chapterNumber = :chapterNumber AND verseNumber = :verseNumber LIMIT 1)")
    suspend fun isBookmarked(bookIndex: Int, chapterNumber: Int, verseNumber: Int): Boolean
}

@Database(entities = [Bookmark::class], version = 1, exportSchema = false)
abstract class BookmarkDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: BookmarkDatabase? = null

        fun getDatabase(context: Context): BookmarkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookmarkDatabase::class.java,
                    "sozo_bible_bookmarks_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
