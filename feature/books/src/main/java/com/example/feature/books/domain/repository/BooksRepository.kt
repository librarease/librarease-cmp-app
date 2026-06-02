package com.example.feature.books.domain.repository

import com.example.core.model.book.BookDetail
import com.example.core.model.book.BookSummary
import com.example.feature.books.domain.model.BooksResult

interface BooksRepository {
    suspend fun getCachedBooks(): List<BookSummary>
    suspend fun getBooks(limit: Int, skip: Int): BooksResult<List<BookSummary>>
    suspend fun getCachedBookDetail(bookId: String): BookDetail?
    suspend fun getBookDetail(bookId: String): BooksResult<BookDetail>
}
