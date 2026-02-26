package com.example.feature.books.data.repository

import android.util.Log
import com.example.feature.books.data.model.toDomain
import com.example.feature.books.data.remote.BooksApiService
import com.example.core.model.book.BookDetail
import com.example.core.model.book.BookSummary
import com.example.feature.books.domain.model.BooksResult
import com.example.feature.books.domain.repository.BooksRepository

class BooksRepositoryImpl(
    private val booksApiService: BooksApiService
) : BooksRepository {

    override suspend fun getBooks(limit: Int): BooksResult<List<BookSummary>> {
        val safeLimit = limit.coerceAtLeast(1)
        return try {
            val books = booksApiService.getBooks(safeLimit).map { dto -> dto.toDomain() }
            BooksResult.Success(books)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load books", e)
            BooksResult.Error(e.message ?: "Failed to load books", e)
        }
    }

    override suspend fun getBookDetail(bookId: String): BooksResult<BookDetail> {
        if (bookId.isBlank()) {
            return BooksResult.Error("Book id is required")
        }

        return try {
            val book = booksApiService.getBookDetail(bookId).toDomain()
            BooksResult.Success(book)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load book detail", e)
            BooksResult.Error(e.message ?: "Failed to load book detail", e)
        }
    }

    companion object {
        private const val TAG = "BooksRepository"
    }
}
