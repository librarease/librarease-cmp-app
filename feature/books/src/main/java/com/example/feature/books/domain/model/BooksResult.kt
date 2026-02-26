package com.example.feature.books.domain.model

sealed class BooksResult<out T> {
    data class Success<T>(val data: T) : BooksResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : BooksResult<Nothing>()
    data object Loading : BooksResult<Nothing>()
}
