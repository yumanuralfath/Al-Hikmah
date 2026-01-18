package com.example.alhikmah.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY lastReadTime DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: String): Flow<Book?>

    @Query("SELECT * FROM books WHERE isFavorite = 1 ORDER BY lastReadTime DESC")
    fun getFavoriteBooks(): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("UPDATE books SET currentPage = :page, lastReadTime = :timestamp WHERE id = :bookId")
    suspend fun updateBookProgress(bookId: Int, page: Int, timestamp: Long)

    //Reading Progress
    @Insert
    suspend fun insertReadingProgress(readingProgress: ReadingProgress)

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getReadingHistory(bookId: Int): Flow<List<ReadingProgress>>
}