package com.example.alhikmah.data

import kotlinx.coroutines.flow.Flow

class BookRepository(private val bookDao: BookDao) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()

    fun getBook(bookId: Int): Flow<Book?> = bookDao.getBookById(bookId)

    fun getFavoriteBooks(): Flow<List<Book>> = bookDao.getFavoriteBooks()

    suspend fun addBook(book: Book): Long {
        return bookDao.insertBook(book)
    }

    suspend fun updateBook(book: Book) {
        bookDao.updateBook(book)
    }

    suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book)
    }

    suspend fun updateReadingProgress(bookId: Int, page: Int){
        bookDao.updateBookProgress(bookId, page, System.currentTimeMillis())
    }

    suspend fun saveReadingSession(bookId: Int, page: Int, percentage: Float, duration: Int){
        val progress = ReadingProgress(
            bookId = bookId,
            pageNumber = page,
            percentage = percentage,
            durationSeconds = duration
        )
        bookDao.insertReadingProgress(progress)
    }

    fun getReadingHistory(bookId: Int): Flow<List<ReadingProgress>>{
        return bookDao.getReadingHistory(bookId)
    }
}