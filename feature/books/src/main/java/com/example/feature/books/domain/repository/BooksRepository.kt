package com.example.feature.books.domain.repository

import com.example.core.model.book.BookDetail
import com.example.core.model.book.BookSummary
import com.example.feature.books.domain.model.BooksResult

interface BooksRepository {
    suspend fun getBooks(limit: Int): BooksResult<List<BookSummary>>
    suspend fun getBookDetail(bookId: String): BooksResult<BookDetail>
}
