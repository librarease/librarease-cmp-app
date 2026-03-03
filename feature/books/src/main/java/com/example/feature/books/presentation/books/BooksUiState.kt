package com.example.feature.books.presentation.books

import com.example.core.model.book.BookSummary

data class BooksUiState(
    val books: List<BookSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isAppending: Boolean = false,
    val errorMessage: String? = null,
    val canLoadMore: Boolean = true
)
